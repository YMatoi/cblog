(ns main.users
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [main.storage :as storage]
            [reagent.core :as reagent]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [bidi.bidi :as bidi]))

(defn component [app-routes]
  (let [users (reagent/atom [:div.article])]
    (go (let [response (<! (http/get "/v1/users"))
              status (:status response)]
          (doseq [user (get-in response [:body :response])]
            (swap! users conj [:a {:href (bidi/path-for app-routes :user-view :id (:id user))}
                               [ui/card
                                [ui/card-header {:title (:name user)
                                                :subtitle (:id user)}]]]))))
    (fn []
      @users)))

