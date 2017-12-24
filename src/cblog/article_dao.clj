(ns cblog.article-dao
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :as helpers]))

(defn article-list [database]
  (let [sql (-> (helpers/select :id :user_id :title :created_at :updated_at)
                (helpers/from :articles)
                sql/format)]
    (jdbc/query (:db-spec database) sql)))

(defn article-create [database data]
  (try (jdbc/insert! (:db-spec database) :articles data)
       (catch Exception e (.getMessage e))))

(defn article-get [database id]
  (let [sql (-> (helpers/select :*)
                (helpers/from :articles)
                (helpers/where [:= :id id])
                sql/format)]
    (println sql)
    (first (jdbc/query (:db-spec database) sql))))

(defn article-update [database data]
  (let [sql (-> (helpers/update :articles)
                (helpers/sset (dissoc data :id))
                (helpers/where [:= :id (:id data)])
                sql/format)]
    (first (jdbc/execute! (:db-spec database) sql))))

(defn article-delete [database id]
  (let [sql (-> (helpers/delete-from :articles)
                (helpers/where [:= :id id])
                sql/format)]
    (println sql)
    (jdbc/execute! (:db-spec database) sql)))
