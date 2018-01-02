(ns main.storage
  (:require [alandipert.storage-atom :as storage]))

(defonce prefs (storage/local-storage (atom {}) :prefs))

(defn set-login-info [user-id token]
  (swap! prefs assoc :token token)
  (swap! prefs assoc :user-id user-id))

(defn unset-login-info []
  (swap! prefs dissoc :token)
  (swap! prefs dissoc :user-id))

(defn get-user-id []
  (:user-id @prefs))

(defn get-token []
  (:token @prefs))
