(ns cblog.app-test
  (:require [bidi.bidi :as bidi]
            [cblog.app :as app])
  (:use clojure.test))

(defn get-handler [url method]
  (:handler (bidi/match-route app/routes url :request-method method)))

(deftest route-test
  (is (= :not-found (get-handler "/test" :get)))
  (is (= :home (get-handler "/" :get)))
  (is (= :not-found (get-handler "/" :post)))
  (is (= :login (get-handler "/login" :post)))
  (is (= :users-list (get-handler "/v1/users" :get)))
  (is (= :user-create (get-handler "/v1/users" :post)))
  (is (= :user-get (get-handler "/v1/users/test" :get)))
  (is (= :user-update (get-handler "/v1/users/test" :put)))
  (is (= :user-delete (get-handler "/v1/users/test" :delete))))
