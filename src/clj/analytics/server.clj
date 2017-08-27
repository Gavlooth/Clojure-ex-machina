(ns analytics.server
  (:require [ring.adapter.jetty :as jetty]
            [analytics.views :as views]
            [analytics.data-operations :refer [data-store]]
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
  (routes
   (GET "/" _ (views/home :data-1 (:data-chart-1 @data-store)))))

(defn  handler []
  (jetty/run-jetty
   (wrap-reload (wrap-defaults
                   (home-routes)  site-defaults))
   {:port 31416 :join? false}))
