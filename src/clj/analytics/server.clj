(ns analytics.server
  (:require [ring.adapter.jetty :as jetty]
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
  (update-data)
  (let [{:keys [data-NA
                correlation-data-matrix
                combined-cancer-data
                correlation-data-labels
                age-significance-data]} @data-store]
  (routes
   (GET "/" _ (views/home
                :data-NA data-NA
                :age-significance-data age-significance-data
                :correlation-data-matrix correlation-data-matrix
                :combined-cancer-data combined-cancer-data
                :correlation-data-labels correlation-data-labels)))))



(defn  handler []
  (jetty/run-jetty
   (wrap-reload (wrap-defaults
                   (#'home-routes)  site-defaults))
   {:port 12345 :join? false}))

(defn -main [] (handler) )
