(ns cblog.server
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty]))

(defrecord Server [port app]
  component/Lifecycle
  (start [this]
    (println ";; Starting Server")
    (if (:server this)
      this
      (assoc this :server (jetty/run-jetty (:app app) {:join? false :port port}))))
  (stop [this]
    (println ";; Stoping Server")
    (println (:server this))
    (if-let [s (:server this)]
      (do (.stop s)
          (println s)
          (dissoc this :server))
      this)))
