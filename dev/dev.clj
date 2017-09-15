(ns dev
  (:require [figwheel-sidecar.repl-api :as fg]
            [com.stuartsierra.component.user-helpers :refer [dev go reset]]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defonce server (atom nil))
(defn run  []
  (fg/start-figwheel!))

(defn stop []
  (fg/stop-figwheel!))

(defn repl []
  (fg/cljs-repl))

