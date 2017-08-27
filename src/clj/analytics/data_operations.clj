(ns analytics.data-operations
  (:require
   [semantic-csv.core :as sc]
   [camel-snake-kebab.core :refer [->kebab-case]]
   [clojure.data.csv :as cd-csv :refer [read-csv]]
   [cognitect.transit :as tr]
   [clojure.java.io :as io]
   [kixi.stats.core
    :refer  [standard-deviation correlation]])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

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
  (:cervical-cancer-risk-classification
   resources))

(def data (with-open [input (io/reader (first  ccrc))]
            (let  [csv (read-csv input)
                   head (first csv)
                   tail (rest csv)]
              (sc/mappify {:keyify false} (doall (cons head tail))))))

(def data (with-open [input (io/reader (first  ccrc))]
            (let  [csv (read-csv input)
                   head (map ->kebab-case (first csv))
                   tail (rest csv)]
              (sc/mappify (doall (cons head tail))))))

#_(defn stat-NA [dt]
    (into {}
          (for [a-key (-> data first keys)
                :let [entries (map #(get % a-key) data)
                      total-entries (count entries)
                      null-entries
                      (count (filter #(= "?" %)
                                     entries))]]
            [a-key {"N/A" null-entries
                    "N/A %" (float
                             (* 100
                                (/ null-entries
                                   total-entries)))}])))

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

(defn update-data []
  (swap! data-store
         assoc :data-chart-1
         (map->transit-json (stat-NA data))))

