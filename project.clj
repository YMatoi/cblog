(defproject cblog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main cblog.core
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [clj-time "0.14.2"]
                 [com.stuartsierra/component "0.3.2"]
                 [ring "1.6.3"]
                 [ring/ring-json "0.4.0"]
                 [bidi "2.1.2"]
                 [buddy "2.0.0"]
                 [funcool/struct "1.1.0"]
                 [hiccup "2.0.0-alpha1"]
                 [honeysql "0.9.1"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [org.postgresql/postgresql "42.1.4"]
                 [danlentz/clj-uuid "0.1.7"]
                 [ragtime "0.7.2"]
                 [reagent "0.8.0-alpha2"]
                 [cljs-react-material-ui "0.2.48"]
                 [reagent-utils "0.2.1"]
                 [alandipert/storage-atom "2.0.1"]
                 [venantius/accountant "0.2.3"]
                 [cljs-http "0.1.44"]]
  :plugins [[lein-cljsbuild "1.1.7"]]
  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/js"]
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler
                        {:main "main.core"
                         :asset-path "/public/js"
                         :output-to "resources/public/js/main.js"
                         :output-dir "resources/public/js"
                         :source-map true
                         :optimizations :none
                         :pretty-print true}}]}
  :profiles
  {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
         :source-paths ["dev"]}})
