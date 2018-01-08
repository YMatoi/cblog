(ns main.articles
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [main.storage :as storage]
            [reagent.core :as reagent]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [bidi.bidi :as bidi]))

(defn component [app-routes]
  (let [articles (reagent/atom [:div.articles])]
    (go (let [response (<! (http/get "/v1/articles"))
              status (:status response)]
          (doseq [article (get-in response [:body :response])]
            (swap! articles conj [:a {:href (bidi/path-for app-routes :article-view :id (:id article))}
                                  [ui/card
                                   [ui/card-header {:title (:title article)
                                                    :subtitle (:user_id article)}]]]))))
    (fn []
      [:div
       @articles])))

