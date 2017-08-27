(ns user
  (:require [figwheel-sidecar.repl-api :as fg]
            [analytics.server :refer [handler]]))

(defonce server (atom nil))
(defn run  []
  (fg/start-figwheel!))

(defn stop []
  (fg/stop-figwheel!))

(defn repl []
  (fg/cljs-repl))

(defn start-server []
  (swap! server assoc :jetty (#'handler)))

(defn stop-server []
  (-> server :jetty (.stop)))
