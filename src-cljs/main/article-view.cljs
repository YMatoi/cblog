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

(defn comments-view [id]
  (let [input (reagent/atom {})
        comments (reagent/atom [:div.comments])]
    (go (let [response (<! (http/get (str "/v1/articles/" id "/comments")))
              status (:status response)
              body (get-in response [:body :response])]
          (cond
            (= 200 status) (doseq [c body]
                             (println (:body c))
                             (println (:user_id c))
                             (swap! comments conj [ui/card
                                                   [ui/card-header {:title (:body c)
                                                                    :subtitle (:user_id c)}]])))))
    (fn []
      [:div
       [:div.input
        [ui/text-field {:floating-label-text "Comment"
                        :on-change #(swap! input assoc :body %2)}]
        [ui/raised-button {:label "Add Comment"
                           :primary true
                           :on-click #(go (let [response (<! (http/post (str "/v1/articles/" id "/comments")
                                                                        {:json-params @input
                                                                         :headers {"Authorization" (str "Token " (storage/get-token))}}))
                                                status (:status response)
                                                title (get-in response [:body :response 0 :body])
                                                user-id (get-in response [:body :response 0 :user_id])]
                                            (cond
                                              (= 200 status)
                                              (do (println response) (swap! comments conj [ui/card
                                                                        [ui/card-header {:title title
                                                                                         :subtitle user-id}]])))))}]]
      @comments]))) 

(defn article-card [body id app-routes]
  [ui/card
   [ui/card-header {:title (:title body)
                    :subtitle (:user_id body)}]
   [ui/card-text [article-body (:body body)]]
   (if (and
           (not (nil? (storage/get-user-id)))
           (= (storage/get-user-id) (:user_id body)))
     [ui/card-actions
      [ui/raised-button {:label "Delete"
                         :secondary true
                         :on-click #(go (let [response (<! (http/delete (str "/v1/articles/" id)
                                                                        {:headers {"Authorization" (str "Token " (storage/get-token))}}))]
                                          (set! (.-location js/document) (bidi/path-for app-routes :articles))))}]
      [ui/raised-button {:label "Edit"
                         :primary true
                         :href (bidi/path-for app-routes :article-update :id id)}]]
     [:div])])

(defn component [app-routes]
  (let [routing-data (session/get :route)
        item (get-in routing-data [:route-params :id])
        article (reagent/atom [:div.article])]
    (go (let [response (<! (http/get (str "/v1/articles/" item)))
              status (:status response)
              body (get-in response [:body :response])]
          (cond
            (= 200 status) (do (swap! article conj [article-card body item app-routes])) 
            :else (do (reset! article [:div "Unknown Error"])))))
    (fn [] 
      [:div
        @article
        [comments-view item]])))
