(ns cblog.app
  (:require [com.stuartsierra.component :as component]
            [bidi.ring :refer [make-handler resources]]
            [ring.util.response :refer [response]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(defn home [req]
  (response {:json "test"}))

(def routes
  ["/" {:get home}])

(def handler
  (make-handler routes))

(def app
  (as-> handler $
    (wrap-json-body $)
    (wrap-json-response $)))

(defrecord App []
  component/Lifecycle
  (start [this]
    (println ";; Starting App")
    (if (:app this)
      this
      (assoc this :app app)))
  (stop [this]
    (println ";; Stopping App")
    (if-let [a (:app this)]
      (dissoc this :app)
      this)))
