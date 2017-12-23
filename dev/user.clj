(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh-all]]
            [com.stuartsierra.component :as component]
            [cblog.core :as core]))

(def system nil)

(defn init []
  (alter-var-root #'system
                  (constantly (core/create-system {:port 3000
                                                   :db-spec "postgresql://postgres:example@localhost:5432/postgres"}))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system (fn [s] (when s (component/stop s)))))

(defn run []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh-all :after 'user/run))
