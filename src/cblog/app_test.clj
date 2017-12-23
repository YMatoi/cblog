(ns cblog.app-test
  (:require [bidi.bidi :as bidi]
            [cblog.app :as app])
  (:use clojure.test))

(deftest test-1
  (is (= (+ 10 10) 20)))
