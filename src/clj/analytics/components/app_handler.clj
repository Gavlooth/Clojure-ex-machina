(ns analytics.components.app-handler
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty]
            [analytics.views :as views]
            [analytics.data-operations :refer
             [data-store update-data map->transit-json]]
            [spyscope.core]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [analytics.data-operations :refer
             [build-corplot-matrix stat-NA extract-labels
              reduced-correlations reduce-cancer-variables]
             :as d-o]
            [compojure.core :refer
             [ANY GET PUT POST DELETE routes]]
            [ring.middleware.defaults :refer
             [api-defaults site-defaults wrap-defaults]]))
;;TODO integrate d-o/ as arguments to component
(defn home-routes []
  (let  [data-NA (map->transit-json (stat-NA d-o/data))
         correlation-data-matrix (map->transit-json d-o/corplot-matrix)
         combined-cancer-data
         (build-corplot-matrix
          reduced-correlations (extract-labels d-o/reduced-data))
         correlation-data-labels (map->transit-json d-o/pure-labels)
         age-significance-data
         (map->transit-json (map #(select-keys % [:age :servical-cancer])
                                 (reduce-cancer-variables d-o/data-coerced)))]
    (routes
     (GET "/" _ (views/home
                 :data-NA data-NA
                 :age-significance-data age-significance-data
                 :correlation-data-matrix correlation-data-matrix
                 :combined-cancer-data combined-cancer-data
                 :correlation-data-labels correlation-data-labels)))))

(defn handler []
  (wrap-reload (wrap-defaults (#'home-routes)  site-defaults)))

(defrecord AppHandler [_]
  component/Lifecycle
  (start [component]
    (if (:handler component) component
      ;; Main app handler
        (assoc component :handler (handler))))
  (stop [component]
    (if-let [handler (:handler component)]
      (assoc component :handler nil)
      component)))

(defn app-handler []
  (map->AppHandler {}))
