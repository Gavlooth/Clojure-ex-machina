(ns analytics.data-operations
  (:require
   [camel-snake-kebab.core :refer [->kebab-case]]
   [clojure.data.csv :as cd-csv :refer [read-csv]]
   [cognitect.transit :as tr]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [kixi.stats.core
    :refer  [standard-deviation correlation correlation-matrix]])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]
           [org.apache.commons.lang3 StringUtils ]))


(defn ->integer?  [x]
  (if  (integer? x)
    x
    (if  (string? x)
      (try
        (Integer/parseInt x)
        (catch Exception e
          :clojure.spec/invalid))
      :clojure.spec/invalid)))

(defn ->double? [x]
  (if  (double? x)
    x
    (if  (string? x)
      (try
        (Double/parseDouble x)
        (catch Exception e
          :clojure.spec/invalid))
      :clojure.spec/invalid)))

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

(def the-keys (map  (comp  keyword ->kebab-case )  (first raw-csv)))


(defn- validize-keywords [st]
 (as-> st  $  (StringUtils/replace $ "(" "<" )
   (StringUtils/replace $ ")" ">" )   (->kebab-case $)))


(defn csv->data [[head & tail]  & {:keys [ns]} ]
  (let  [legend (mapv #(if ns (keyword  ns (validize-keywords %))
                         (keyword  (validize-keywords %))) head)]
    (mapv  zipmap (repeat legend) tail)))

(def data (csv->data raw-csv ))



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

(def legend  (keys (first data)))


(def data-fixed  (csv->data  csv-fixed))

(def freq (for [a-key legend]
           {a-key  (frequencies (map a-key data-fixed))}))

(-> data-fixed first vals)

;; (def continuous-variables :smokes-(years) )
(defn  descrite-variables  )

(defn update-data []
  (swap! data-store
         assoc :data-chart-1
         (map->transit-json (stat-NA data))))

