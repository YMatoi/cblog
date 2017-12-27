(ns cblog.comment
  (:require [cblog.utils :refer [defhandler validate]]
            [cblog.dao :as dao]
            [cblog.response :as response]
            [struct.core :as s]))

(def routes
  {:get :comments-list
   :post :comment-create
   "/" {[:id] {:get :comment-get
               :put :comment-update
               :delete :comment-delete}}})

(defhandler comments-list [req]
  (response/ok (dao/comments-list (:database req))))
