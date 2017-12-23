(ns cblog.utils
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]))

(defmacro defhandler [name args & body]
  `(defn ~(vary-meta name assoc :ring-handler (keyword name)) ~args ~@body))

(defn ring-handler? [var]
  (contains? (meta var) :ring-handler))

(defn ring-handlers []
  (->> (all-ns)
       (mapcat ns-interns)
       (map second)
       (filter ring-handler?)))

;; migration
(defn load-config [db-spec]
  {:datastore (jdbc/sql-database db-spec)
   :migrations (jdbc/load-resources "migrations")})

(defn migrate [db-spec]
  (repl/migrate (load-config db-spec)))

(defn rollback [db-spec]
  (repl/rollback (load-config db-spec)))
