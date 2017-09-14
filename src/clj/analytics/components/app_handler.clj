(ns analytics.components.app-handler
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty]
            [analytics.views :as views]
            [analytics.data-operations :refer [data-store update-data]]
            [spyscope.core]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [compojure.core :refer
             [ANY GET PUT POST DELETE routes]]
            [ring.middleware.defaults :refer
             [api-defaults
              site-defaults
              wrap-defaults]]))

(defn home-routes []
  (let  [data-NA (map->transit-json (stat-NA data))
        correlation-data-matrix (map->transit-json corplot-matrix)
        combined-cancer-data (build-corplot-matrix reduced-correlations (extract-labels reduce-data))
        correlation-data-labels (map->transit-json pure-labels)
        age-significance-data (map->transit-json
                                       (map
                                        #(select-keys % [:age :servical-cancer])
                                        (reduce-cancer-variables data-coerced)))]
    (routes
     (GET "/" _ (views/home
                 :data-NA data-NA
                 :age-significance-data age-significance-data
                 :correlation-data-matrix correlation-data-matrix
                 :combined-cancer-data combined-cancer-data
                 :correlation-data-labels correlation-data-labels)))))


(defrecord AppHandler [app-config postgres-db]
  component/Lifecycle
  (start [component]
    (if (:handler component) component
      ;; Main app handler
        (assoc component :handler handler)))
  (stop [component]
    (if-let [handler (:handler component)]
      (assoc component :handler nil)
      component)))

(defn app-handler []
  (map->AppHandler {}))
