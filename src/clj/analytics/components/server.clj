(ns analytics.components.server
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty]))

#_(defn home-routes []
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


#_(defrecord AppHandler [app-config postgres-db]
  component/Lifecycle
  (start [component]
    (if (:handler component) component
      ;; Main app handler
        (assoc component :handler handler)))
  (stop [component]
    (if-let [handler (:handler component)]
      (assoc component :handler nil)
      component)))

#_(defn app-handler []
  (map->AppHandler {}))

#_(defrecord Server [app-config app-handler]
  component/Lifecycle
  (start [component]
    (if (:server component)
      component
      (let [options (get-in app-config [:options :server])
            options (assoc options :join? false)
            handler (:handler app-handler)
            server (jetty/run-jetty handler options)]
        (assoc component :server server))))
  (stop [component]
    (if-let [server (:server component)]
      (do (.stop server)
          (.join server)
          (assoc component :server nil))
      component)))

