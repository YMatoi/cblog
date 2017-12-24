(ns cblog.utils
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [ring.util.response :as resp]
            [struct.core :as s]
            [buddy.core.hash :as hash]
            [buddy.core.codecs :as codecs]))

(defmacro defhandler [name args & body]
  (let [msg (gensym)]
    `(defn ~(vary-meta name assoc :ring-handler (keyword name)) ~args
       (try
         ~@body
         (catch Exception ~msg
           (println (str "caught exception: " (.getMessage ~msg)))
           (resp/status {} 500))))))

(defn response [error response status-code]
  (resp/status (resp/response {:error error
                               :response response}) status-code))

(def id-pattern
  {:message "only letters (a-z or A-Z), numbers (0-9)"
   :optional true
   :validate (fn [v]
               (and (string? v)
                    (some? (re-matches #"^[a-zA-Z0-9]+$" v))))})

(defn validate [data validator]
  (try
    (s/validate data validator)
    (catch Exception msg
      (println (str "caught exception: " (.getMessage msg)))
      '("bad request" nil))))

(defn sha256 [password]
  (-> (hash/sha256 password)
      (codecs/bytes->hex)))

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
