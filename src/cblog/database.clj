(ns cblog.database
  (:require [com.stuartsierra.component :as component]))

(defrecord Database [db-spec]
  component/Lifecycle
  (start [this]
    (println ";; Starting Database")
    (assoc this :db-spec db-spec))
  (stop [this]
    (println ";; Stopping Database")
    (dissoc this :db-spec)))
