(ns cblog.core
  (:require [com.stuartsierra.component :as component]
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
              (server/map->Server {:port port})
              {:app :app}))))

(def db-spec "postgresql://postgres:example@localhost:5432/postgres")

(def system (create-system {:port 3000
                            :db-spec db-spec}))

(defn -main []
  (component/start system))
