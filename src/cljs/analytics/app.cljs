(ns analytics.app
  (:require [dommy.core :as dm]
            [cognitect.transit :as t])
  (:import [goog.string]))

(enable-console-print!)

(def data-el  (dm/parent (dm/sel1 :#bar-chart-1)))

(defn parse-data-tag [el data-tag]
  (let [rdr (t/reader :json)]
    (t/read rdr (dm/attr el data-tag))))

(def data (parse-data-tag data-el  "data-bar-chart-1"))

(defn plot-NAs [el labels NAs NAs-%]
  (let [bar-data [{:x labels
                   :y  NAs
                   :type "bar"
                   :text  NAs-%
                   :textposition "auto"
                   :hoverinfo "none"
                   ;; :orientation "h"
                   :marker  {:color "rgb (158,202,225)"
                             :opacity  0.6,
                             :line  {:color "rbg (8,48,107)" :width 1.5}}}]
        layout {:title  "Data  NAs" :margin {:b 150}  :xaxis {:tickangle 30} }]
    (.newPlot js/Plotly el  (clj->js bar-data)  (clj->js layout))))

(def labels (map #(clojure.string/replace
                   (name %)
                   "-" " ")  (keys data)))
(def NAs (map  :N/A   (vals data)))

(def NAs-% (map  #(str (.substr (str  (:N/A-% %)) 0 5) "%")   (vals data)))
(plot-NAs (dm/sel1 :#bar-chart-1) labels NAs NAs-%)
