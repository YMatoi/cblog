(ns cblog.app
  (:require [com.stuartsierra.component :as component]
            [bidi.ring :refer [make-handler resources Ring]]
            [bidi.bidi :as bidi]
            [ring.util.response :refer [response status]]
            [hiccup2.core :as hiccup]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [cblog.utils :refer [sha256 defhandler ring-handler? ring-handlers validate id-pattern]]
            [cblog.user :as user]
            [cblog.user-dao :as dao]
            [buddy.sign.jwt :as jwt]
            [buddy.core.nonce :as nonce]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer [jwe-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [clj-time.core :as time]
            [struct.core :as s]))

(defhandler home [req]
  (if-not (authenticated? req)
    (throw-unauthorized)
    (response (str (hiccup/html [:div#foo.bar.baz "bang"])))))

(defhandler not-found [req]
  (status (response nil) 404))

(def secret (nonce/random-bytes 32))

(def login-json
  {:id [[s/required]
        [id-pattern]
        [s/max-count 64]]
   :password [[s/required]
              [s/min-count 8]
              [s/max-count 256]]})

(defhandler login [req]
  (let [[result validated] (validate (get-in req [:body]) login-json)]
    (if (nil? result)
      (let [valid? (= (sha256 (:password validated))
                      (:password (dao/user-get (:database req) (:id validated))))]
        (if valid?
          (let [claims {:user (keyword (:id validated))
                        :exp (time/plus (time/now) (time/seconds 3600))}
                token (jwt/encrypt claims secret {:alg :a256kw :enc :a128gcm})]
            {:status 200 :body {:token token}})
          (status (response nil) 400)))
      (status (response nil) 400))))

(def auth-backend (jwe-backend {:secret secret
                                :options {:alg :a256kw :enc :a128gcm}}))

(def routes
  ["/" {:get :home
        "login" {:post :login}
        "v1" {"/users" user/routes}
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

(defn app [database]
  (as-> (make-handler routes) $
    (wrap-authorization $ auth-backend)
    (wrap-authentication $ auth-backend)
    (wrap-database $ database)
    (wrap-params $)
    (wrap-json-body $ {:keywords? true})
    (wrap-json-response $)))

(defrecord App [database]
  component/Lifecycle
  (start [this]
    (println ";; Starting App")
    (if (:app this)
      this
      (assoc this :app (app database))))
  (stop [this]
    (println ";; Stopping App")
    (if-let [a (:app this)]
      (dissoc this :app)
      this)))
