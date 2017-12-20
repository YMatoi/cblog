(ns cblog.core
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer (refresh)]
            [cblog.database :as database]
            [cblog.app :as app]
            [cblog.server :as server]))

(defn create-system [config-options]
  (let [{:keys [port db-spec]} config-options]
    (println port db-spec)
    (component/system-map
     :db (database/map->Database {:db-spec db-spec})
     :app (app/map->App {})
     :server (component/using
              (server/new-server port)
              [:app]))))

(def system nil)

(defn init []
  (alter-var-root #'system
                  (constantly (create-system {:port 3000
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
  (refresh :after 'cblog.core/run))
