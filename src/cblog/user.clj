(ns cblog.user
  (:require [cblog.utils :refer [defhandler validate sha256 id-pattern]]
            [cblog.response :as response]
            [cblog.user-dao :as dao]
            [struct.core :as s]))

(def routes
  {:get :users-list
   :post :user-create
   "/" {[:id] {:get :user-get
               :put :user-update
               :delete :user-delete}}})

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

(defhandler users-list [req]
  (response/ok (dao/user-list (:database req))))

(defhandler user-create [req]
  (let [[result validated] (validate (get-in req [:body]) create-json)]
    (if (nil? result)
      (let [created (dao/user-create
                     (:database req)
                     (assoc validated :password (sha256 (:password validated))))]
        (if (nil? (:id (first created)))
          (response/conflict {:id "already used"} (dissoc validated :password))
          (response/ok (dissoc validated :password))))
      (response/bad-request result (dissoc validated :password)))))

(defhandler user-get [req]
  (let [id (get-in req [:params :id])]
    (if-let [data (dao/user-get (:database req) id)]
      (if (= (:user (:identity req)) id)
        (response/ok (dissoc data :password)) ; 自分自身をgetした場合はaddressを表示する
        (response/ok (dissoc data :address :password))) ; 他のユーザーをgetした場合はaddressを表示しない
      (response/not-found {:id "is not found"}))))

(defhandler user-update [req {:auth? true}]
  (let [id (get-in req [:params :id])
        data (get-in req [:body])]
    (if (= (:identity req) id)
      (let [validated (validate data update-json)]))))

(defhandler user-delete [req {:auth? true}]
  (if-let [id (get-in req [:params :id])]
    (if (= (:user (:identity req)) id)
      (if (nil? (dao/user-get (:database req) id))
        (response/not-found {:id "is not found"})
        (response/ok (dao/user-delete (:database req) id)))
      (response/bad-request "bad request" nil))))
