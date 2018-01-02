(ns main.article-create
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [main.storage :as storage]
            [reagent.core :as reagent]
            [reagent.session :as session]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [bidi.bidi :as bidi]))

(defn component [app-routes]
  (let [input (reagent/atom {})
        errors (reagent/atom {})]
    (fn []
      [ui/paper {:style {:padding "10px"}}
       [ui/text-field {:floating-label-text "Title"
                       :full-width true
                       :on-change #(swap! input assoc :title %2)
                       :error-text (:title errors)}]
       [:br]
       [ui/text-field {:floating-label-text "Body"
                       :full-width true
                       :on-change #(swap! input assoc :body %2)
                       :error-text (:body errors)
                       :multi-line true}]
       [:br]
       [ui/flat-button {:label "Post"
                        :on-click #(go (let [response (<! (http/post "/v1/articles" {:json-params @input
                                                                                     :headers {"Authorization" (str "Token " (storage/get-token))}}))
                                             status (:status response)]
                                        (cond
                                          (= 200 status) (set! (.-location js/document) (bidi/path-for app-routes :article-view :id (get-in response [:body :response 0 :id])))
                                          (= 400 status) (reset! errors (get-in response [:body :errors])))))}]]))) 

