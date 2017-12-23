(ns cblog.utils)

(defmacro defhandler [name args & body]
  `(defn ~(vary-meta name assoc :ring-handler (keyword name)) ~args ~@body))

(defn ring-handler? [var]
  (contains? (meta var) :ring-handler))

(defn ring-handlers []
  (->> (all-ns)
       (mapcat ns-interns)
       (map second)
       (filter ring-handler?)))
