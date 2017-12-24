(ns cblog.user
  (:require [cblog.utils :refer [defhandler response validate sha256 id-pattern]]
            [cblog.user-dao :as dao]
            [struct.core :as s]))

(def routes
  {:get :users-list
   :post :user-create
   "/" {[:id] {:get :user-get
               :put :user-update
               :delete :user-delete}}})

(def login-json
  {:id [[s/required]
        [id-pattern]
        [s/max-count 64]]
   :password [[s/required]
              [s/min-count 8]
              [s/max-count 256]]})

(defhandler users-list [req]
  (response nil (dao/user-list (:database req)) 200))

(def create-json
  {:id [[s/required]
        [id-pattern]
        [s/max-count 64]]
   :password [[s/required]
              [s/min-count 8]
              [s/max-count 256]]
   :address [[s/required]
             [s/max-count 256]
             [s/email]]
   :name [[s/required]
          [s/max-count 64]]
   :profile [[s/max-count 256]]})

(def update-json
  {:password [[s/min-count 8]
              [s/max-count 256]]
   :address [[s/max-count 256]
             [s/email]]
   :name [[s/max-count 64]]
   :profile [[s/max-count 256]]})

(defhandler user-create [req]
  (let [[result validated] (validate (get-in req [:body]) create-json)]
    (if (nil? result)
      (let [created (dao/user-create
                     (:database req)
                     (assoc validated :password (sha256 (:password validated))))]
        (if (nil? (:id (first created)))
          (response {:id "already used"} (dissoc validated :password) 409)
          (response nil nil  200)))
      (response result (dissoc validated :password) 400))))

(defhandler user-get [req]
  (let [id (get-in req [:params :id])]
    (if-let [data  (dao/user-get (:database req) id)]
      (response nil data 200)
      (response {:id "is not found"} nil 404))))

(defhandler user-update [req]
  (let [id (get-in req [:params :id])
        data (get-in req [:body])]
    (let [validated (validate data update-json)])))

(defhandler user-delete [req]
  (if-let [id (get-in req [:params :id])]
    (if (nil? (dao/user-get (:database req) id))
      (response {:id "is not found"} nil 404)
      (response nil (dao/user-delete (:database req) id) 200))))
