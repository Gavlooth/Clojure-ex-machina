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
           [org.apache.commons.lang3 StringUtils]))

(defn ->?integer  [x]
  (if  (integer? x)
    x
    (if  (string? x)
      (try
        (Integer/parseInt x)
        (catch Exception e
          :clojure.spec/invalid))
      :clojure.spec/invalid)))

(defn ->?double [x]
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
    (str out)))

(def resources
  (let [dirs    (.listFiles (io/file "resources/data/"))]
    (into {}   (map #(vector
                      (keyword (.getName %))
                      (.listFiles %)) dirs))))

(def ccrc
  (:cervical-cancer-risk-classification   resources))

(def raw-csv (-> ccrc first io/reader read-csv))

(defn- restructure-keyword [st]
  (as-> st  $  (StringUtils/replace $ "(" "<")
        (StringUtils/replace $ ")" ">")   (->kebab-case $)))

(def the-keys (map (comp keyword  ->kebab-case restructure-keyword)   (first raw-csv)))

(defn csv->data [[head & tail]  & {:keys [ns]}]
  (let  [legend (mapv #(if ns (keyword  ns (restructure-keyword %))
                           (keyword  (restructure-keyword %))) head)]
    (mapv  zipmap (repeat legend) tail)))

(def data (csv->data raw-csv))

(s/def ::age  ->?integer)
(s/def ::number-of-sexual-partners ->?integer)
(s/def ::first-sexual-intercourse ->?integer)
(s/def ::num-of-pregnancies  ->?integer)
(s/def ::smokes  ->?integer)
(s/def ::smokes-<years>  ->?double)
(s/def ::smokes-<packs/year>  ->?double)
(s/def ::hormonal-contraceptives  ->?integer)
(s/def ::hormonal-contraceptives-<years>  ->?double)
(s/def ::iud  ->?integer)
(s/def ::iud-<years>  ->?integer)
(s/def ::st-ds  ->?integer)
(s/def ::st-ds-<number>  ->?integer)
(s/def ::st-ds:condylomatosis ->?integer)
(s/def ::st-ds:cervical-condylomatosis ->?integer)
(s/def ::st-ds:vaginal-condylomatosis ->?integer)
(s/def ::st-ds:vulvo-perineal-condylomatosis ->?integer)
(s/def ::st-ds:syphilis ->?integer)
(s/def ::st-ds:pelvic-inflammatory-disease ->?integer)
(s/def ::st-ds:genital-herpes ->?integer)
(s/def ::st-ds:molluscum-contagiosum ->?integer)
(s/def ::st-ds:-aids ->?integer)
(s/def ::st-ds:-hiv ->?integer)
(s/def ::st-ds:-hepatitis-b ->?integer)
(s/def ::st-ds:-hpv ->?integer)
(s/def ::st-ds:-number-of-diagnosis ->?integer)
(s/def ::st-ds:-time-since-first-diagnosis ->?integer)
(s/def ::st-ds:-time-since-last-diagnosis ->?integer)
(s/def ::dx:-cancer ->?integer)
(s/def ::dx:-cin ->?integer)
(s/def ::dx:-hpv ->?integer)
(s/def ::dx ->?integer)
(s/def ::hinselmann ->?integer)
(s/def ::schiller ->?integer)
(s/def ::citology ->?integer)
(s/def ::biopsy ->?integer)

(s/def ::data
  (s/keys :req-un
          [::age  ::number-of-sexual-partners
           ::first-sexual-intercourse
           ::num-of-pregnancies  ::smokes
           ::smokes-<years>  ::smokes-<packs/year>
           ::hormonal-contraceptives
           ::hormonal-contraceptives-<years>
           ::iud  ::iud-<years>  ::st-ds
           ::st-ds-<number>
           ::st-ds:condylomatosis
           ::st-ds:cervical-condylomatosis
           ::st-ds:vaginal-condylomatosis
           ::st-ds:vulvo-perineal-condylomatosis
           ::st-ds:syphilis
           ::st-ds:pelvic-inflammatory-disease
           ::st-ds:genital-herpes
           ::st-ds:molluscum-contagiosum
           ::st-ds:-aids ::st-ds:-hiv
           ::st-ds:-hepatitis-b ::st-ds:-hpv
           ::st-ds:-number-of-diagnosis
           ::st-ds:-time-since-first-diagnosis
           ::st-ds:-time-since-last-diagnosis
           ::dx:-cancer ::dx:-cin ::dx:-hpv ::dx
           ::hinselmann ::schiller ::citology
           ::biopsy]))

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
;; (defn  descrite-variables  )

(defn update-data []
  (swap! data-store
         assoc :data-chart-1
         (map->transit-json (stat-NA data))))

