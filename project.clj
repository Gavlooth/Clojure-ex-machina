(defproject analytics "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/clojurescript
                  "1.9.908" :scope "provided"]
                 [org.clojure/data.csv "0.1.4"]
                 [camel-snake-kebab "0.4.0"]
                 [ring "1.6.2"]
                 [prismatic/dommy "1.1.0"]
                 [ring/ring-defaults "RELEASE"]
                 [semantic-csv "0.2.1-alpha1"]
                 [compojure "1.6.0"]
                 [cheshire "5.8.0"]
                 [philoskim/debux "0.3.12"]
                 [hiccup "RELEASE"]
                 [com.cognitect/transit-clj "0.8.300"]
                 [org.apache.commons/commons-lang3 "3.6"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [com.stuartsierra/component.repl "0.2.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [kixi/stats "0.3.9"]
                 [org.apache.spark/spark-mllib_2.10 "2.2.0"]]
  :plugins [[lein-cljsbuild "1.1.6"]
            [lein-environ "1.1.0"]]



  :min-lein-version "2.6.1"

  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/js"]

  :uberjar-name "analytics.jar"

;;  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/cljs"]
  :main analytics.core

  ;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;; because that's where our development helper functions like (go) and
  ;; (browser-repl) live.
  :repl-options {:init-ns user }
;  :figwheel
  :cljsbuild {:builds
              [{:id "app"
                :source-paths ["src/cljs" "src/cljc"]
                :figwheel true
                :compiler {:main analytics.app
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/analytics.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}
               {:id "min"
                :source-paths ["src/cljs" "src/cljc"]
                :jar true
                :compiler {:main analytics.app
                           :output-to "resources/public/js/analytics.js"
                           :output-dir "target"
                           :source-map-timestamp true
                           :optimizations :advanced
                           :pretty-print false}}]}

  :profiles {:dev
             {:dependencies [[figwheel "0.5.11"]
                             [figwheel-sidecar "0.5.11"]
                             [com.cemerick/piggieback "0.2.2"]
                             [org.clojure/tools.nrepl "0.2.13"]
                             [reloaded.repl "0.2.3"]]

              :plugins [[lein-figwheel "0.5.11"]
                        [lein-doo "0.1.7"]]

              :source-paths ["dev"]
              :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}

             :uberjar
             {:source-paths ^:replace ["src/clj" "src/cljc"]
              :prep-tasks ["compile"
                           ["cljsbuild" "once" "min"]]
              :hooks []
              :omit-source true
              :aot :all}})
