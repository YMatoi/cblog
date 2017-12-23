(ns cblog.user
  (:require [cblog.utils :refer [defhandler]]
            [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]
            [ring.util.response :refer [response]]))

(def routes
  {:get :users-list
   :post :user-create
   "/" {[:id] {:get :user-get
               :put :user-update
               :delete :user-delete}}})

(defn create [database data]
  (try (jdbc/insert! (:db-spec database) :users data)
       (catch Exception e (.getMessage e))))

(defn index [database]
  (let [sql (sql/format {:select [:id :name]
                         :from [:users]})]
    (jdbc/query (:db-spec database) sql)))

(defhandler users-list [req]
  (response (index (:database req))))

(defhandler user-create [req]
  (let [{:keys [id name address password]} (get-in req [:body])
        data {:id id :name name :address address :password password}]
    (response (create (:database req) data))))

(defhandler user-get [req]
  (let [id (get-in req [:params :id])]
    (response {:user "get"
               :id id})))

(defhandler user-update [req]
  (let [id (get-in req [:params :id])
        body (get-in req [:body])]
    (response {:user "update"
               :id id
               :body body})))

(defhandler user-delete [req]
  (let [id (get-in req [:params :id])]
    (response {:user "delete"
               :id id})))
