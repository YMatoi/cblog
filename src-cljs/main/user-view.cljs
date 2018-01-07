(ns main.user-view
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [main.storage :as storage]
            [reagent.core :as reagent]
            [reagent.session :as session]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [bidi.bidi :as bidi]))

(defn user-view [user-id]
  (let [user (reagent/atom [:div.user])]
    (go (let [response (<! (http/get (str "/v1/users/" user-id)))
              status (:status response)
              body (get-in response [:body :response])]
          (reset! user [ui/card
                        [ui/card-header {:title (:name body)
                                         :subtitle (:id body)}]
                        [ui/card-text (:profile body)]])))
    (fn []
      @user)))

(defn articles-view [user-id]
  (let [articles (reagent/atom [:div.articles])]
    (go (let [response (<! (http/get (str "/v1/articles" "?user-id=" user-id)))
              status (:status response)]
          (doseq [article (get-in response [:body :response])]
            (swap! articles conj [:a {:href (str "/articles/id-" (:id article))} 
                                  [ui/card
                                   [ui/card-header {:title (:title article)}]]]))))
    (fn []
     @articles))) 


(defn component [app-routes]
  (let [routing-data (session/get :route)
        id (get-in routing-data [:route-params :id])]
    (fn []
      [:div
       [user-view id]
       [articles-view id]])))
