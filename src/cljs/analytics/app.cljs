(ns analytics.app
  (:require [dommy.core :as dm]
            [debux.cs.core :refer-macros [clog clogn break] ]
            [cognitect.transit :as t])

  (:import [goog.string]))

(enable-console-print!)


(def data-NA-element (dm/parent (dm/sel1 :#bar-chart-NA)))


(defn parse-data-tag [el data-tag]
  (let [rdr (t/reader :json)]
    (t/read rdr (dm/attr el data-tag))))

(def data-NA (parse-data-tag data-NA-element "data-bar-chart-NA"))

(defn plot-NAs [el labels NAs NAs-%]
  (let [bar-data [{:x labels
                   :y  NAs
                   :type "bar"
                   :text  NAs-%
                   :textposition "auto"
                   :hoverinfo "none"
                   ;; :orientation "h"
                   :marker  {:color "rgb (158,202,225)"
                             :opacity  0.6
                             :line  {:color "rbg (8,48,107)" :width 1.5}}}]
        layout {:title  "Data  NAs"
                :margin {:b 150}
                :xaxis {:tickangle 30}}]
    (.newPlot js/Plotly el  (clj->js bar-data)  (clj->js layout))))

(def data-NA-labels
  (map #(clojure.string/replace (name %) "-" " ")  (keys data-NA)))
(def NAs (map  :N/A   (vals data-NA)))

(def NAs-% (map  #(str (.substr (str  (:N/A-% %)) 0 5) "%")  
                (vals data-NA)))
(plot-NAs (dm/sel1 :#bar-chart-NA) data-NA-labels NAs NAs-%)

(def correlation-data-element (dm/parent (dm/sel1 :#correlation-heatmap)))

(def data-correlation-matrix  (parse-data-tag correlation-data-element
                             "data-heatmap-correlation-matrix" ))

(def data-correlation-labels  (parse-data-tag correlation-data-element
                             "data-heatmap-correlation-labels" ))

(def correlation-data-matrix
  (clj->js  data-correlation-matrix))

(def correlation-data-labels (clj->js data-correlation-labels))
(.log js/console correlation-data-labels)


(.plot  js/Plotly correlation-data-element
       (clj->js  [{:colorscale [[0 "#B22222"]
                                [1 "#20B2AA"]] 
                   :z (reverse correlation-data-matrix)
                   :x  correlation-data-labels 
                   :y (reverse  correlation-data-labels)     
                   :xgap 2
                   :ygap 2
                   :type "heatmap"}])
     (clj->js  { :title "Correlations bettween variables"
                :width 1000
                :height 1000
                :margin {:t 300
                         :l 150 }
                :xaxis {:side "top"
                        :tickangle -30}}))
