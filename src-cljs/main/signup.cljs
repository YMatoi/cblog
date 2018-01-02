(ns main.signup
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
        errors (reagent/atom {})]
    (fn []
      [ui/paper {:style {:width "276px"
                         :margin "auto"
                         :padding "10px"}}
       [ui/text-field {:floating-label-text "UserID"
                       :on-change #(swap! input assoc :id %2)
                       :error-text (:id @errors)}]
       [:br]
       [ui/text-field {:floating-label-text "UserName"
                       :on-change #(swap! input assoc :name %2)
                       :error-text (:name @errors)}]
       [:br]
       [ui/text-field {:floating-label-text "EMail Address"
                       :on-change #(swap! input assoc :address %2)
                       :type "email"
                       :error-text (:address @errors)}]
       [:br]
       [ui/text-field {:floating-label-text "Password"
                       :on-change #(swap! input assoc :password %2)
                       :type "password"
                       :error-text (:password @errors)}]
       [:br]
       [ui/text-field {:floating-label-text "Profile"
                       :on-change #(swap! input assoc :profile  %2)
                       :error-text (:profile @errors)
                       :multi-line true}]
       [:br]
       [ui/flat-button {:label "SignUp"
                        :on-click #(go (let [response (<! (http/post "/v1/users" {:json-params @input}))
                                             status (:status response)]
                                         (println response)
                                         (cond
                                           (= 200 status) (set! (.-location js/document) (bidi/path-for app-routes :signin))
                                           (or (= 400 status) (= 409 status)) (reset! errors (get-in response [:body :errors])))))}]])))

