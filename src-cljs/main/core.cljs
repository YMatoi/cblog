(ns main.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
            [reagent.session :as session]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :refer [get-mui-theme color]]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-react-material-ui.icons :as ic]
            [bidi.bidi :as bidi]
            [accountant.core :as accountant]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [main.signin :as signin]
            [main.signup :as signup]
            [main.articles :as articles]
            [main.article-view :as article-view]
            [main.article-update :as article-update]
            [main.article-create :as article-create]
            [main.storage :as storage]))

(enable-console-print!)

(def app-routes
  ["/" {"" :index
        "signin" :signin ; ログイン
        "signout" :signout ; ログアウト
        "signup" :signup ; 登録
        "articles" {"" :articles ; 記事一覧
                    ["/id-" :id] :article-view
                    ["/id-" :id "/edit"] :article-update
                    "/create" :article-create}
        "users" {"" :users
                 ["/" :id] :user-view}}]) ;ユーザー記事一覧 

(defmulti page-contents identity)

(defmethod page-contents :index []
  (articles/component app-routes))

(defmethod page-contents :signin []
  (signin/component app-routes))

(defmethod page-contents :signup []
  (signup/component app-routes))

(defmethod page-contents :signout []
  (storage/unset-login-info)
  (set! (.-location js/document) (bidi/path-for app-routes :index)))

(defmethod page-contents :articles []
  (articles/component app-routes))

(defmethod page-contents :article-view []
  (article-view/component app-routes))

(defmethod page-contents :article-update []
  (article-update/component app-routes))

(defmethod page-contents :article-create []
  (article-create/component app-routes))

(defmethod page-contents :users []
  (fn []
    [:span
     [:h1 "users"]]))

(defmethod page-contents :user-view []
  (let [routing-data (session/get :route)
        item (get-in routing-data [:route-params :id])
        articles (reagent/atom [:div.article])]
    (fn []
      (when (<= (count @articles) 1)
        (println "hote"))
      @articles)))

(defmethod page-contents :default []
  (fn []
    [:span
     [:h1 "404"]]))

(defn right-menu []
  (if-let [user-id (storage/get-user-id)]
    [:div 
      [ui/raised-button {:label "SignOut"
                         :secondary true
                         :href (bidi/path-for app-routes :signout)}]]
    [:div
      [ui/raised-button {:label "SignIn"
                         :primary true
                         :href (bidi/path-for app-routes :signin)}]
      [ui/raised-button {:label "SignUp"
                         :secondary true
                         :href (bidi/path-for app-routes :signup)}]]))

(defn page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [ui/mui-theme-provider
       {:mui-theme (get-mui-theme)}
       [:div
        [ui/app-bar {:title (storage/get-user-id)
                     :style {:max-width "800px"
                             :margin-left "auto"
                             :margin-right "auto"}
                     :class-name "header"
                     :icon-element-left (reagent/as-element [ui/icon-button {:tooltip "Home" :href (bidi/path-for app-routes :index)} (ic/action-home)])
                     :icon-element-right (reagent/as-element [right-menu])
                     :icon-style-right {:margin-top "auto"
                                        :margin-bottom "auto"}}]
        [:div.contents
         ^{:key page} [page-contents page]]]])))

(defn on-js-reload []
  (reagent/render-component [page] (.getElementById js/document "app")))

(defn init []
  (accountant/configure-navigation!
    {:nav-handler (fn [path]
                    (let [match (bidi/match-route app-routes path)
                          current-page (:handler match)
                          route-params (:route-params match)]
                      (session/put! :route {:current-page current-page
                                            :route-params route-params})))
     :path-exist? (fn [path]
                    (boolean (bidi/match-route app-routes path)))})
  (accountant/dispatch-current!)
  (on-js-reload))

(set! (.-onload js/window) init)
