(ns cblog.app
  (:require [com.stuartsierra.component :as component]
            [bidi.ring :refer [make-handler resources Ring]]
            [bidi.bidi :as bidi]
            [hiccup2.core :as hiccup]
            [hiccup.page :as page]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [response]]
            [cblog.utils :refer [sha256 defhandler ring-handler? ring-handlers validate id-pattern]]
            [cblog.user :as user]
            [cblog.dao :as dao]
            [cblog.article :as article]
            [cblog.response :as response]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [clj-time.core :as time]
            [bidi.ring :refer [resources]]
            [struct.core :as s]))

(defhandler home [req {:auth? false}]
  (response (str (hiccup/html [:head (page/include-js "/public/js/main.js")
                               (page/include-css "/public/css/main.css")]
                              [:div#app]))))

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
                      (:password (dao/users-get (:database req) (:id validated))))]
        (if valid?
          (response/ok ((:encrypt req) (:id validated)))
          (response/bad-request "authentication failed" nil)))
      (response/bad-request "authentication failed" nil))))

(def routes
  ["/" {"v1" {"/users" user/routes
              "/articles" article/routes
              "/login" {:post :login}}
        "public" (resources {:prefix "public"})
        true :home}])

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
