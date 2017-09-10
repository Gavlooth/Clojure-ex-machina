 (ns analytics.views
   (:require  [hiccup.page :refer  [html5 include-css]]
              [hiccup.util :refer [to-uri]]
              [spyscope.core]))

(defn head []
  [:head
   [:title "Clojure data analysis!!"]
   [:meta  {:charset "UTF-8"}]
   [:meta  {:name "viewport"
            :content
            (str "width=device-width, "
                 "initial-scale=1, "
                 " minimum-scale=1, "
                 " maximum-scale=1")}]
   [:title  "Clojure Analytics"]
   (include-css  "css/style.css")
   (include-css "css/bulma.css")])

(defn home  [& {:keys [data-NA
                       correlation-data-labels
                       correlation-data-matrix
                       cancer-classes
                       ]}]
  (html5
   {:lang "en"}
   (head)
   [:body
    [:div.container
     [:div.column.is-6.is-offset-3]
     [:div.row {:style {:height "800"} :data-bar-chart-NA data-NA}
      [:div#bar-chart-NA]]
     [:div.row {:style {:height "800"}
                :data-heatmap-correlation-matrix correlation-data-matrix 
                :data-heatmap-correlation-labels correlation-data-labels}
      [:div#correlation-heatmap]]
     
     [:div.row {:style {:height "800"} :data-bar-chart-cancer-classes
                                      cancer-classes }
            [:div#bar-chart-cancer-classes ]] ]
    [:script  {:type "application/javascript"
               :src  (to-uri "/plotly-latest.min.js")}]
    [:script  {:type "application/javascript"
               :src  (to-uri "/js/analytics.js")}]]))

