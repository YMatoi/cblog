(ns cblog.core
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer (refresh)]
            [cblog.database :as database]
            [cblog.app :as app]
            [cblog.auth :as auth]
            [cblog.utils :as utils]
            [cblog.server :as server]
            [environ.core :refer [env]]))

(defn create-system [config-options]
  (let [{:keys [port db-spec]} config-options]
    (println port db-spec)
    (component/system-map
     :database (database/map->Database {:db-spec db-spec})
     :auth (auth/map->Auth {})
     :app (component/using
           (app/map->App {})
           [:database :auth])
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
  (let [config {:port (Long/parseLong (env :port))
                :db-spec (env :db-spec)}]
    (utils/migrate (:db-spec config))
    (component/start (create-system config))))
