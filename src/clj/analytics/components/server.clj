(ns analytics.components.server
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty]))





(defrecord Server [app-config app-handler]
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

(defn server []
  (map->Server {}))
