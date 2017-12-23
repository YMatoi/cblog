(ns cblog.app
  (:require [com.stuartsierra.component :as component]
            [bidi.ring :refer [make-handler resources Ring]]
            [bidi.bidi :as bidi]
            [ring.util.response :refer [response]]
            [hiccup2.core :as hiccup]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [cblog.utils :refer [defhandler ring-handler? ring-handlers]]
            [cblog.user :as user]))

(defhandler home [req]
  (println (:database req))
  (response (str (hiccup/html [:div#foo.bar.baz "bang"]))))

(defhandler articles [req]
  (response {:test "articles"}))

(def routes
  ["/" {:get :home
        "v1" {"/users" user/routes
              "/articles" {:get :articles}}}])

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
    (wrap-database $ database)
    (wrap-params $)
    (wrap-json-body $)
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
