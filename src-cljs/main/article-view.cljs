(ns main.article-view
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [main.storage :as storage]
            [reagent.core :as reagent]
            [reagent.session :as session]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [bidi.bidi :as bidi]))

(defn article-body [body]
  (let [html (reagent/atom [:div])]
    (fn []
      (doseq [line (clojure.string/split-lines body)]
        (swap! html conj [:p line]))
      @html))) 

(defn component [app-routes]
  (let [routing-data (session/get :route)
        item (get-in routing-data [:route-params :id])
        article (reagent/atom [:div.article])]
    (fn []
      (when (<= (count @article) 1)
        (go (let [response (<! (http/get (str "/v1/articles/" item)))
                  status (:status response)
                  body (get-in response [:body :response])]
              (cond
                (= 200 status) (do (swap! article conj [ui/card 
                                                        [ui/card-header {:title (:title body)
                                                                         :subtitle (:user_id body)}]
                                                        [ui/card-text [article-body (:body body)]]])
                                   (when (= (storage/get-user-id) (:user_id body))
                                     (swap! article conj [ui/flat-button {:label "Delete" :secondary true
                                                                          :on-click #(go (let [response (<! (http/delete (str "/v1/articles/" item) {:headers {"Authorization" (str "Token " (storage/get-token))}}))]
                                                                                           (set! (.-location js/document) (bidi/path-for app-routes :articles))))}])
                                     (swap! article conj [ui/flat-button {:label "Edit"
                                                                          :href (bidi/path-for app-routes :article-update :id item)}])))
                :else (do (reset! article [:div "Unknown Error"]))))))
      @article)))
