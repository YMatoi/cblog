(ns cblog.auth
  (:require [com.stuartsierra.component :as component]
            [buddy.sign.jwt :as jwt]
            [buddy.core.nonce :as nonce]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer [jwe-backend]]
            [clj-time.core :as time]
            [struct.core :as s]))

(defrecord Auth [auth-backend encrypt]
  component/Lifecycle
  (start [this]
    (let [secret (nonce/random-bytes 32)
          options {:alg :a256kw :enc :a128gcm}]
      (println ";; Starting Auth")
      (if (:auth-backend this)
        this
        (assoc this
               :auth-backend (jwe-backend {:secret secret :options options})
               :encrypt (fn [user-id]
                          (let [claims {:user (keyword user-id)
                                        :exp (time/plus (time/now) (time/seconds 36000))}
                                token (jwt/encrypt claims secret options)]
                            token))))))
  (stop [this]
    (println ";; Stopping Auth")
    (if (:auth-backend this)
      (dissoc this :auth-backend :encrypt)
      this)))
