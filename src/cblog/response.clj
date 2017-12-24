(ns cblog.response)

(defn ok [d]
  {:status 200 :body {:response d}})

(defn not-found [d]
  {:status 404 :body {:errors d}})

(defn conflict [msg d]
  (let [body {:errors msg}]
    (if d
      {:status 409 :body (assoc body :response d)}
      {:status 409 :body body})))

(defn bad-request [msg d]
  (let [body {:errors msg}]
    (if d
      {:status 400 :body (assoc body :response d)}
      {:status 400 :body body})))
