(ns main.article-update
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [main.storage :as storage]
            [reagent.core :as reagent]
            [reagent.session :as session]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [bidi.bidi :as bidi]))

(defn article-update [app-routes title body item]
  (let [input (reagent/atom {:title body :body body})
        errors (reagent/atom {})]
    (fn []
      [ui/paper {:style {:padding "10px"}}
       [ui/text-field {:floating-label-text "Title"
                       :full-width true
                       :default-value title
                       :on-change #(swap! input assoc :title %2)}]
       [:br]
       [ui/text-field {:floating-label-text "Body"
                       :default-value body
                       :full-width true
                       :multi-line true
                       :on-change #(swap! input assoc :body %2)}]
       [:br]
       [ui/flat-button {:label "Update"
                        :on-click #(go (let [response (<! (http/put (str "/v1/articles/" item) {:json-params @input
                                                                                                :headers {"Authorization" (str "Token " (storage/get-token))}}))
                                             status (:status response)]
                                         (cond
                                           (= 200 status) (set! (.-location js/document) (bidi/path-for app-routes :article-view :id item))
                                           (= 400 status) (reset! errors (get-in response [:body :errors])))))}]])))

(defn component [app-routes]
  (let [routing-data (session/get :route)
        item (get-in routing-data [:route-params :id])
        update-form (reagent/atom [:div])]
    (fn []
      (go (let [response (<! (http/get (str "/v1/articles/" item)))
                status (:status response)
                title (get-in response [:body :response :title])
                body (get-in response [:body :response :body])]
            (reset! update-form (article-update app-routes title body item))))
      @update-form)))
