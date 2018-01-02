(ns main.signin
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [main.storage :as storage]
            [reagent.core :as reagent]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [bidi.bidi :as bidi]))

(defn component [app-routes]
  (let [input (reagent/atom {})
        error (reagent/atom "")]
    (fn []
      [ui/paper {:style {:width "276px"
                         :margin "auto"
                         :padding "10px"}}
        [ui/text-field {:floating-label-text "UserID"
                        :on-change #(swap! input assoc :id %2)
                        :error-text @error}]
        [:br]
        [ui/text-field {:floating-label-text "Password"
                        :type "password"
                        :on-change #(swap! input assoc :password %2)
                        :error-text @error}]
        [:br]
        [ui/flat-button {:label "SignIn"
                         :on-click #(go (let [response (<! (http/post "/v1/login" {:json-params @input}))
                                              status (:status response)]
                                          (cond
                                            (= 200 status) (do 
                                                             (storage/set-login-info (:id @input) (get-in response [:body :response]))
                                                             (set! (.-location js/document) (bidi/path-for app-routes :articles)))
                                            (= 400 status) (reset! error (get-in response [:body :errors]))
                                            :else (do (println "hoge") (reset! error "Unknown Error")))))}]])))
