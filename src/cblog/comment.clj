(ns cblog.comment
  (:require [cblog.utils :refer [defhandler validate]]
            [cblog.dao :as dao]
            [cblog.response :as response]
            [struct.core :as s]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [clj-uuid :as uuid]))

(def routes
  {:get :comments-list
   :post :comment-create
   "/" {[:id] {:get :comment-get
               :put :comment-update
               :delete :comment-delete}}})

(def comment-json
  {:body [[s/required]]
   :parent_id [[s/max-count 36]]})

(defhandler comments-list [req]
  (let [article-id (get-in req [:params :id])]
    (response/ok (dao/comments-list (:database req) [:= :article_id article-id]))))

(defhandler comment-create [req {:auth? true}]
  (let [user-id (:user (:identity req))
        article-id (get-in req [:params :id])
        [result validated] (validate (get-in req [:body]) comment-json)]
    (if (and (nil? result)
             (or (nil? (:parent_id validated))
                 (not (nil? (dao/comments-get (:database req) (:parent_id validated))))))
      (let [id (uuid/v1)
            created_at (coerce/to-long (time/now))
            created (dao/comments-create (:database req)
                                         {:id id
                                          :user_id user-id
                                          :article_id article-id
                                          :parent_id (:parent_id validated)
                                          :body (:body validated)
                                          :created_at created_at
                                          :updated_at created_at})]
        (if-let [data created]
          (response/ok data)
          (response/bad-request "bad request" nil)))
      (response/bad-request result validated))))
