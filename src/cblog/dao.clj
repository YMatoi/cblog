(ns cblog.dao
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :as helpers]))

(defn create-function-name [table-name postfix]
  (symbol (str (name table-name) postfix)))

(defmacro gen-dao-list [table-name & columns]
  (let [fn-list (create-function-name table-name "-list")
        database (gensym)
        where (gensym)
        sql (gensym)]
    `(defn ~fn-list [~database ~where]
       (let [~sql (-> (helpers/select ~@columns)
                      (helpers/from ~table-name)
                      (helpers/where ~where)
                      sql/format)]
         (println ~sql)
         (jdbc/query (:db-spec ~database) ~sql)))))

(defmacro gen-dao-create [table-name]
  (let [fn-create (create-function-name table-name "-create")
        database (gensym)
        data (gensym)
        sql (gensym)
        e (gensym)]
    `(defn ~fn-create [~database ~data]
       (try (jdbc/insert! (:db-spec ~database) ~table-name ~data)
            (catch Exception ~e (.getMessage ~e))))))

(defmacro gen-dao-get [table-name & columns]
  (let [fn-get (create-function-name table-name "-get")
        database (gensym)
        id (gensym)
        sql (gensym)]
    `(defn ~fn-get [~database ~id]
       (let [~sql (-> (helpers/select ~@columns)
                      (helpers/from ~table-name)
                      (helpers/where [:= :id ~id])
                      sql/format)]
         (first (jdbc/query (:db-spec ~database) ~sql))))))

(defmacro gen-dao-update [table-name]
  (let [fn-update (create-function-name table-name "-update")
        database (gensym)
        data (gensym)
        id (gensym)
        sql (gensym)]
    `(defn ~fn-update [~database ~data]
       (let [~sql (-> (helpers/update ~table-name)
                      (helpers/sset (dissoc ~data :id))
                      (helpers/where [:= :id (:id ~data)])
                      sql/format)]
         (first (jdbc/execute! (:db-spec ~database) ~sql))))))

(defmacro gen-dao-delete [table-name]
  (let [fn-delete (create-function-name table-name "-delete")
        database (gensym)
        id (gensym)
        sql (gensym)]
    `(defn ~fn-delete [~database ~id]
       (let [~sql (-> (helpers/delete-from ~table-name)
                      (helpers/where [:= :id ~id])
                      sql/format)]
         (jdbc/execute! (:db-spec ~database) ~sql)))))

(gen-dao-list :users :id :name :profile)
(gen-dao-create :users)
(gen-dao-get :users :id :name :profile :address :password)
(gen-dao-delete :users)

(gen-dao-list :articles :*)
(gen-dao-create :articles)
(gen-dao-get :articles :*)
(gen-dao-update :articles)
(gen-dao-delete :articles)

(gen-dao-list :comments :*)
(gen-dao-create :comments)
(gen-dao-get :comments :*)
(gen-dao-update :comments)
(gen-dao-delete :comments)
