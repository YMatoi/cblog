(ns cblog.user-dao
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :as helpers]))

(defn user-list [database]
  (let [sql (-> (helpers/select :id :name :profile)
                (helpers/from :users)
                sql/format)]
    (jdbc/query (:db-spec database) sql)))

(defn user-create [database data]
  (try (jdbc/insert! (:db-spec database) :users data)
       (catch Exception e (.getMessage e))))

(defn user-get [database id]
  (let [sql (-> (helpers/select :id :name :profile :address :password)
                (helpers/from :users)
                (helpers/where [:= :id id])
                sql/format)]
    (first (jdbc/query (:db-spec database) sql))))

(defn user-delete [database id]
  (let [sql (-> (helpers/delete-from :users)
                (helpers/where [:= :id id])
                sql/format)]
    (println sql)
    (jdbc/execute! (:db-spec database) sql)))
