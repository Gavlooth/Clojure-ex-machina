(ns analytics.app
  (:require [dommy.core :as dm]
            [goog.string :as gstring]
            [goog.string.format]
            [debux.cs.core :refer-macros [clog clogn break]]
            [cognitect.transit :as t]))

(enable-console-print!)

;;The element tha holds the data tag
(def data-NA-element (dm/parent (dm/sel1 :#bar-chart-NA)))

;; Transit reader
(defn parse-data-tag [el data-tag]
  "Parse the data from a transit atribute value"
  (let [rdr (t/reader :json)]
    (t/read rdr (dm/attr el data-tag))))

(def data-NA (parse-data-tag data-NA-element "data-bar-chart-NA"))

;; TODO generalize plotly.js wrappers for reusability
(defn plot-NAs [el labels NAs NAs-%]
  "Display the NAs values in a plotly.js bargraph"
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
                                              "data-heatmap-correlation-matrix"))

(def data-correlation-labels  (parse-data-tag correlation-data-element
                                              "data-heatmap-correlation-labels"))
;; For better visualability remove the uper triangel from the matrix
(def lower-triangular
  (vec (map-indexed
        (fn  [i x]
          (vec  (map-indexed
                 (fn [j y] (when-not (< i j) y)) x)))
        data-correlation-matrix)))

(def correlation-data-matrix
  (clj->js  data-correlation-matrix))

(def correlation-data-labels (clj->js data-correlation-labels))

;; FIXME remove the function call and generalize with a wrapper
(.plot  js/Plotly correlation-data-element
        (clj->js  [{:colorscale [[0 "#B22222"]
                                 [1 "#20B2AA"]]
                    :z (reverse lower-triangular)
                    :x  correlation-data-labels
                    :y (reverse  correlation-data-labels)
                    :xgap 2
                    :ygap 2
                    :type "heatmap"}])
        (clj->js  {:title "Correlations bettween variables"
                   :width 1000
                   :height 1000
                   :margin {:t 300
                            :l 230}
                   :xaxis {:side "top"
                           :tickangle -40}}))

(defn matrix->lower-triangular  [the-matrix]
  (vec (map-indexed
        (fn  [i x]
          (vec  (map-indexed
                 (fn [j y] (when-not (< i j) y)) x)))
        the-matrix)))

(defn corrplot [correlation-data-element
                correlation-data-labels
                the-matrix
                title]
(let [lower-triangular (matrix->lower-triangular the-matrix)]
  (.plot  js/Plotly correlation-data-element
          (clj->js  [{:colorscale [[0 "#B22222"]
                                   [1 "#20B2AA"]]
                      :z (reverse lower-triangular)
                      :x  correlation-data-labels
                      :y (reverse  correlation-data-labels)
                      :xgap 2
                      :ygap 2
                      :type "heatmap"}])
          (clj->js  {:title title
                     :width 1000
                     :height 1000
                     :margin {:t 300
                              :l 230}
                     :xaxis {:side "top"
                             :tickangle -35}}))))



(def combined-cancer-element (dm/parent (dm/sel1 :#chart-combined-cancer)))


(let [the-matrix (parse-data-tag combined-cancer-element "data-combined-cancer-matrix")
             the-labels  (parse-data-tag  combined-cancer-element "data-combined-cancer-labels")]
  (corrplot (dm/sel1 :#chart-combined-cancer) the-labels the-matrix  "Correlation with combined test result variable \"cervical cancer\"" ))


(defn plot-stack-bar [el labels values hover-text title]
  "Display the NAs values in a plotly.js bargraph"
  (let [bar-data [{:y labels
                   :x values
                   :orientation "h"
                   :type "bar"
                   :text hover-text
                   :textposition "auto"
                   :hoverinfo "none"
                   :marker  {:color "rgb (158,202,225)"
                             :opacity  0.6
                             :line  {:color "rbg (8,48,107)" :width 2}}}]
        layout {:title title
                :height 1000
                :margin {:l 260}}]
    (.newPlot js/Plotly el  (clj->js bar-data)  (clj->js layout))))


(let [the-labels (drop-last (parse-data-tag  combined-cancer-element "data-combined-cancer-labels"))
           the-data   (last  (parse-data-tag combined-cancer-element "data-combined-cancer-matrix"))
          hover-text (map #(gstring/format "%.2f" %) the-data)]
  (plot-stack-bar  (dm/sel1 :#bar-chart-combined-cancer) the-labels the-data hover-text "Positive cancer and variable correlation" ))
