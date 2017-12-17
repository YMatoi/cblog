(ns cblog.database
  (:require [com.stuartsierra.component :as component]))

(defrecord Database [db-spec]
  component/Lifecycle
  (start [this]
    (println ";; Starting Database")
    (assoc this :connection db-spec))
  (stop [this]
    (println ";; Stopping Database")
    (dissoc this :connection db-spec)))
