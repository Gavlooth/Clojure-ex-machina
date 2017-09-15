
(ns analytics.core
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl :refer [set-init]]
            [analytics.components
             [app-handler :refer [app-handler]]
             [server :refer [server]]]))



(defn -main [& args]
  (let [prod-system
        (component/system-map
         :app-handler (component/using
                       (app-handler) [])
         :server (component/using
                  (server) [:app-handler]))]
    (component/start prod-system)))

(set-init -main)
