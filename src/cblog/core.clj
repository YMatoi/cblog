(ns cblog.core
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer (refresh)]
            [cblog.database :as database]
            [cblog.app :as app]
            [cblog.utils :as utils] 
            [cblog.server :as server]))

(defn create-system [config-options]
  (let [{:keys [port db-spec]} config-options]
    (println port db-spec)
    (component/system-map
     :database (database/map->Database {:db-spec db-spec})
     :app (component/using
           (app/map->App {})
           [:database])
     :server (component/using
              (server/map->Server {:port port})
              [:app]))))

(def config-options
  {:port 3000
   :db-spec "postgresql://postgres:example@localhost:5432/postgres"})

(defn migrate []
  (utils/migrate (:db-spec config-options)))

(defn rollback []
  (utils/rollback (:db-spec config-options)))

(defn -main []
  (create-system config-options))
