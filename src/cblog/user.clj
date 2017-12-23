(ns cblog.user
  (:require [cblog.utils :refer [defhandler]]
            [ring.util.response :refer [response]]))

(def routes
  {:get :users-list
   :post :user-create
   "/" {[:id] {:get :user-get
               :put :user-update
               :delete :user-delete}}})

(defhandler users-list [req]
  (response {:user "list"}))

(defhandler user-create [req]
  (let [body (get-in req [:body "nothig"])]
    (response {:user "create"
               :body body})))

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
