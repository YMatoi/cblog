(ns cblog.server
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty]))

(defrecord Server [port app server]
  component/Lifecycle
  (start [this]
    (println ";; Starting Server")
    (println port)
    (if (:server this)
      this
      (assoc this :server (jetty/run-jetty (:app app) {:join? false :port port}))))
  (stop [this]
    (println ";; Stopping Server")
    (if-let [s (:server this)]
      (do (.stop s)
          (dissoc this :server))
      this)))
