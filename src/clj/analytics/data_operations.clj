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


(def ->?double
  (s/conformer  (fn [x]
                  (cond
                    (integer? x) x
                    (string? x)  (try
                                   (Double/parseDouble x)
                                   (catch Exception _
                                     :clojure.spec.alpha/invalid))
                    :else :clojure.spec.alpha/invalid))))


(def ->?integer
  (s/conformer
   (fn  [x]
     (cond
       (integer? x) x
       (string? x)  (try
                      (Integer/parseInt (StringUtils/removeEnd  x ".0"))
                      (catch Exception _
                        :clojure.spec.alpha/invalid))
       :else :clojure.spec.alpha/invalid))))

;; Storage for the transit data
(defonce data-store (atom {:data-NA nil :corr-data nil}))


(defn map->transit-json [a-map]
 "Convert a transit string from a map"
  (let [out  (ByteArrayOutputStream. 4096)
        writer  (tr/writer out :json)]
    (tr/write writer a-map)
    (str out)))

;; Load the resources. TODO move data from resources to data/ folder
(def resources
  (let [dirs    (.listFiles (io/file "resources/data/"))]
    (into {}   (map #(vector
                      (keyword (.getName %))
                      (.listFiles %)) dirs))))

(def ccrc
  (:cervical-cancer-risk-classification   resources))

(def raw-csv (-> ccrc first io/reader read-csv))

(defn- restructure-keyword [st]
 "Convert strings to valid clojure keywords"
  (as-> st  $  (StringUtils/replace $ "(" "<")
        (StringUtils/replace $ ")" ">") ;replace () with <>
        (StringUtils/replace $ "/" "-per-") ; replace / with -per-
        (->kebab-case $)))

(def the-keys (map (comp keyword  ->kebab-case restructure-keyword) 
                   (first raw-csv)))

(defn csv->data [[head & tail]  & {:keys [ns]}]
  "csv data to clojure vector of maps (dataframe)"
  (let  [labels (mapv #(if ns (keyword  ns (restructure-keyword %))
                           (keyword  (restructure-keyword %))) head)]
    (mapv  zipmap (repeat labels) tail)))

(def data (csv->data raw-csv))

;; Change "?" to -1 for data exploation. TODO merge with clojure.spec and generalize 
;;FIXME For  contiouus data use  median
(def csv-fixed
  (map #(map  (fn [x] (if  (= x "?") -1 x))  %)  raw-csv))

(def labels  (vec  (keys (first data)))) ;TODO Create function

(def pure-labels (first raw-csv))

(def data-fixed  (csv->data  csv-fixed))
;;; Data specs
(s/def ::age  ->?integer)
(s/def ::number-of-sexual-partners ->?integer)
(s/def ::first-sexual-intercourse ->?integer)
(s/def ::num-of-pregnancies  ->?integer)
(s/def ::smokes  ->?integer)
(s/def ::smokes-<years>  ->?double)
(s/def ::smokes-<packs-per-year>  ->?double)
(s/def ::hormonal-contraceptives  ->?integer)
(s/def ::hormonal-contraceptives-<years>  ->?double)
(s/def ::iud  ->?integer)
(s/def ::iud-<years>  ->?double) ;corected
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
           ::smokes-<years>  ::smokes-<packs-per-year>
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

;;;Data exploration section

;; Data Coercion TODO Check data for :clojure.spec.alpha/invalid  values
(defn coerce-csv [data]
 "Use clojure.spec to coerse" 
  (map #(s/conform ::data %) data))

;; Before coercing the data, use this to validate it
(defn check-data [data] 
  (map #(s/explain ::data %) data)) 

(check-data data-fixed)

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

(def data-coerced
  (coerce-csv data-fixed))


;; The deferent cervical cancer classes
(def cervical-cancer-classes
 (for [datum  data-coerced ]
  (select-keys datum
               [:hinselmann :schiller
                       :citology :biopsy]))) 

;lets tacke a pick of value frequencies in files
(zipmap [:hinselmann :schiller :citology :biopsy] (map frequencies (map  #(map (fn [x] (get x %) ) cervical-cancer-classes)   [:hinselmann :schiller :citology :biopsy]))) ;;frequencies in classes

;now we can compine the categorial variables 


(defn calculate-overall-proporsions [data] 
 (let [overall  (reduce
                  #(+ % (reduce
                          (fn [x [k v]]
                            (+ x v)) 0 %2))
                  0 data)
       
       the-keys (keys (first data))]  
(zipmap the-keys 
   (map  #(/ (reduce (fn [acc  el]
                  (+ acc  (get  el %))) 0 data) overall) the-keys))))


(calculate-overall-proporsions cervical-cancer-classes)




(def freq (for [a-key labels]
            {a-key  (frequencies (map a-key data-coerced))}))

;; (calculate-overall-proporsions  data-coerced )
(def correlations
  (transduce
    identity
      (correlation-matrix 
       (zipmap labels labels)) 
      data-coerced))


;(reduce (fn [x [k v] ] (+ x v)) 0 (first data-coerced))
;; To
(def indexies
  (map (fn [[[x y] z]]
         [(.indexOf labels x) ]) correlations ))


(defn map-indexies [labels correlations]
 "Create a matrix (vector of vectors) with position indexies
 instead of keys."
  (let [get-index #(.indexOf labels %)]
  (->> correlations
   (map (fn [[[x y] z]]
         [[(get-index x)
           (get-index y) ]  z]))
   (concat (mapv (fn [x] [ [x x] 1] ) (range 36)))
   (sort-by  (comp  second first))
   (sort-by ffirst))))

;;Build a regular indexed matrix to handle the data visualization with plotly.js 
(def corplot-matrix
(->> correlations
   (map-indexies labels)
   (map  (fn [[[x y] z ]] z))
   (partition 36) ) )

;;Updates the data storage atom with appropriate transits
(defn update-data []
  (swap! data-store
         assoc :data-NA
         (map->transit-json (stat-NA data)))

  (swap! data-store
         assoc :correlation-data-matrix
         (map->transit-json corplot-matrix))

(swap! data-store
         assoc :correlation-data-labels
         (map->transit-json pure-labels)))



