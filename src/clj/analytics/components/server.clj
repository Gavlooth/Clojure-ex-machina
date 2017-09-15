(ns analytics.components.server
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty]))

;; this will be implemented in the dev folder
#_(defrecord Figwheel []
    component/Lifecycle
    (start []
      (ra/start-figwheel!))
    (stop []
      (ra/stop-figwheel!)))



(defrecord Server [app-handler]
  component/Lifecycle
  (start [component]
    (if (:server component)
      component
      (let [handler (:handler app-handler)
            server (jetty/run-jetty handler {:port 12345 :join? false})]
        (assoc component :server server))))
  (stop [component]
    (if-let [server (:server component)]
      (do (.stop server)
          (.join server)
          (assoc component :server nil))
      component)))

(defn server []
  (map->Server {}))
