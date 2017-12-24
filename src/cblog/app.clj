(ns cblog.app
  (:require [com.stuartsierra.component :as component]
            [bidi.ring :refer [make-handler resources Ring]]
            [bidi.bidi :as bidi]
            [hiccup2.core :as hiccup]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [response]]
            [cblog.utils :refer [sha256 defhandler ring-handler? ring-handlers validate id-pattern]]
            [cblog.user :as user]
            [cblog.user-dao :as dao]
            [cblog.article :as article]
            [cblog.response :as response]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [clj-time.core :as time]
            [struct.core :as s]))

(defhandler home [req {:auth? true}]
  (response (str (hiccup/html [:div#foo.bar.baz "bang"]))))

(defhandler not-found [req]
  (response/not-found "path is not found"))

(def login-json
  {:id [[s/required]
        [id-pattern]
        [s/max-count 64]]
   :password [[s/required]
              [s/min-count 8]
              [s/max-count 256]]})

(defhandler login [req]
  (let [[result validated] (validate (get-in req [:body]) login-json)]
    (println (:encrypt req))
    (if (nil? result)
      (let [valid? (= (sha256 (:password validated))
                      (:password (dao/user-get (:database req) (:id validated))))]
        (if valid?
          (response/ok ((:encrypt req) (:id validated)))
          (response/bad-request "authentication failed" nil)))
      (response/bad-request "authentication failed" nil))))

(def routes
  ["/" {:get :home
        "login" {:post :login}
        "v1" {"/users" user/routes
              "/articles" article/routes}
        true :not-found}])

(defn match-handler [k]
  (->> (ring-handlers)
       (filter #(= (:ring-handler (meta %))
                   k))
       first))

(extend-protocol Ring
  clojure.lang.Keyword
  (request [k req _]
    (let [handler (match-handler k)]
      (handler req))))

(defn wrap-database [f database]
  (fn [req]
    (f (assoc req :database database))))

(defn wrap-auth [f encrypt]
  (fn [req]
    (println encrypt)
    (f (assoc req :encrypt encrypt))))

(defn app [database auth]
  (as-> (make-handler routes) $
    (wrap-auth $ (:encrypt auth))
    (wrap-authorization $ (:auth-backend auth))
    (wrap-authentication $ (:auth-backend auth))
    (wrap-database $ database)
    (wrap-params $)
    (wrap-json-body $ {:keywords? true})
    (wrap-json-response $)))

(defrecord App [database auth]
  component/Lifecycle
  (start [this]
    (println ";; Starting App")
    (if (:app this)
      this
      (assoc this :app (app database auth))))
  (stop [this]
    (println ";; Stopping App")
    (if-let [a (:app this)]
      (dissoc this :app)
      this)))
