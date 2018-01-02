(ns cblog.article
  (:require [cblog.utils :refer [defhandler validate]]
            [cblog.response :as response]
            [cblog.dao :as dao]
            [cblog.comment :as comment]
            [struct.core :as s]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [clj-uuid :as uuid]))

(def routes
  {:get :articles-list
   :post :article-create
   "/" {[:id] {:get :article-get
               :put :article-update
               :delete :article-delete
               "/comments" comment/routes}}})

(def article-json
  {:title [[s/required]
           [s/max-count 64]]
   :body [[s/required]
          [s/max-count 10000]]})

(defhandler articles-list [req]
  (response/ok (dao/articles-list (:database req) nil)))

(defhandler article-create [req {:auth? true}]
  (if-let [user-id (:user (:identity req))]
    (let [[result validated] (validate (get-in req [:body]) article-json)]
      (if (nil? result)
        (let [id (uuid/v1)
              created_at (coerce/to-long (time/now))
              created (dao/articles-create (:database req)
                                           {:id id
                                            :user_id user-id
                                            :title (:title validated)
                                            :body (:body validated)
                                            :created_at created_at
                                            :updated_at created_at})]
          (if-let [data created]
            (response/ok data)
            (response/bad-request "bad request" nil)))
        (response/bad-request result validated)))
    (response/bad-request "authenticated failed" nil)))

(defhandler article-update [req {:auth? true}]
  (if-let [id (get-in req [:params :id])]
    (if-let [user-id (:user (:identity req))]
      (let [[result validated] (validate (get-in req [:body]) article-json)]
        (if (nil? result)
          (let [updated_at (coerce/to-long (time/now))
                created (dao/articles-update (:database req)
                                             {:id id
                                              :title (:title validated)
                                              :body (:body validated)
                                              :updated_at updated_at})]
            (if-let [data (dao/articles-get (:database req) id)]
              (response/ok data)
              (response/bad-request "bad request" nil)))
          (response/bad-request result validated)))
      (response/bad-request "authenticated failed" nil))))

(defhandler article-get [req]
  (let [id (get-in req [:params :id])]
    (if-let [data (dao/articles-get (:database req) id)]
      (response/ok data)
      (response/not-found {:id "is not found"}))))

(defhandler article-delete [req {:auth? true}]
  (if-let [id (get-in req [:params :id])]
    (if (= (:user (:identity req))
           (:user_id (dao/articles-get (:database req) id)))
      (response/ok (dao/articles-delete (:database req) id))
      (response/bad-request "bad request" nil))))
