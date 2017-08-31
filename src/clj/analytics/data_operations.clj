(ns analytics.data-operations
  (:require
   [semantic-csv.core :as sc]
   [camel-snake-kebab.core :refer [->kebab-case]]
   [clojure.data.csv :as cd-csv :refer [read-csv]]
   [cognitect.transit :as tr]
   [clojure.java.io :as io]
   [kixi.stats.core
    :refer  [standard-deviation correlation correlation-matrix]])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]
           [org.apache.commons.lang3 StringUtils ]))


(defonce data-store (atom {:data-chart-1 nil}))

(defn map->transit-json [a-map]
  (let [out  (ByteArrayOutputStream. 4096)
        writer  (tr/writer out :json)]
    (tr/write writer a-map)
    (.toString out)))

(def resources
  (let [dirs    (.listFiles (io/file "resources/data/"))]
    (into {}   (map #(vector
                      (keyword (.getName %))
                      (.listFiles %)) dirs))))

(def ccrc
  (:cervical-cancer-risk-classification   resources))

(def raw-csv (-> ccrc first io/reader read-csv))

(defn par->brak [s]
(StringUtils/replace (StringUtils/replace s "(" "<" ) ")" ">" ))

(defn csv->data [ [head & tail] ]
  (let  [new-head (map  #(par->brak (->kebab-case %)) head) ]
  (vec (sc/mappify (doall (cons new-head tail))))))

(def data (csv->data raw-csv))

(defn stat-NA [dt]
  (into {}
        (for [a-key (-> data first keys)
              :let [entries (map #(get % a-key) data)
                    total-entries (count entries)
                    null-entries  (count
                                   (filter #(= "?" %)
                                           entries))]]
          [a-key {:N/A null-entries
                  :N/A-% (float
                          (* 100  (/ null-entries
                                     total-entries)))}])))

(def csv-fixed
  (map #(map  (fn [x] (if  (= x "?") -1 x))  %)  raw-csv))

(def data-fixed  (csv->data  csv-fixed))

(par->brak "hormonal-contraceptives-(years)" )

;; (transduce identity (correlation-matrix  (into {} (map #(vector % %) (keys (first data-fixed))))) data-fixed )

(defn update-data []
  (swap! data-store
         assoc :data-chart-1
         (map->transit-json (stat-NA data))))

