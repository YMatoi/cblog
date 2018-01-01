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
            [alandipert.storage-atom :as storage]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)

(def app-routes
  ["/" {"" :index
        "signin" :signin ; ログイン
        "signout" :signout ; ログアウト
        "signup" :signup ; 登録
        "articles" {"" :articles ; 記事一覧
                    ["/" :id] :article-view
                    ["/" :id "/edit"] :article-update}
        "users" {"" :users
                 ["/" :id] :user-view}}]) ;ユーザー記事一覧 

(defonce prefs (storage/local-storage (atom {}) :prefs))

(defn set-login-info [user-id token]
  (swap! prefs assoc :token token)
  (swap! prefs assoc :user-id user-id))

(defn unset-login-info []
  (swap! prefs dissoc :token)
  (swap! prefs dissoc :user-id))

(defmulti page-contents identity)

(defmethod page-contents :index []
  (fn []
    [:span
     [:h1 "index"]]))

(defmethod page-contents :signin []
  (let [input (reagent/atom {})
        error (reagent/atom "")]
    (fn []
      [:div.form
        [ui/text-field {:hint-text "UserID"
                        :on-change #(swap! input assoc :id %2)
                        :error-text @error}]
        [ui/text-field {:hint-text "Password"
                        :type "password"
                        :on-change #(swap! input assoc :password %2)
                        :error-text @error}]
        [ui/flat-button {:label "SignIn"
                         :onClick #(go 
                                     (let [response (<! (http/post "/v1/login" {:json-params @input}))
                                           status (:status response)]
                                       (cond
                                         (= 200 status) (set-login-info @user-id (get-in response [:body :response])) 
                                         (= 400 status) (reset! error (get-in response [:body :errors]))
                                         :else (do (println "hoge") (reset! error "Unknown Error")))))}]])))


(defmethod page-contents :signup []
  (let [input (reagent/atom {})
        errors (reagent/atom {})]
    (fn []
      [:div.form
       [ui/text-field {:hint-text "UserID"
                       :on-change #(swap! input assoc :id %2)
                       :error-text (:id @errors)}]
       [ui/text-field {:hint-text "UserName"
                       :on-change #(swap! input assoc :name %2)
                       :error-text (:name @errors)}]
       [ui/text-field {:hint-text "EMail Address"
                       :on-change #(swap! input assoc :address %2)
                       :type "email"
                       :error-text (:address @errors)}]
       [ui/text-field {:hint-text "Password"
                       :on-change #(swap! input assoc :password %2)
                       :type "password"
                       :error-text (:password @errors)}]
       [ui/text-field {:hint-text "Profile"
                       :on-change #(swap! input assoc :profile  %2)
                       :error-text (:profile @errors)
                       :multiLine true}]
       [ui/flat-button {:label "SignUp"
                        :onClick #(go (let [response (<! (http/post "/v1/users" {:json-params @input}))
                                            status (:status response)]
                                        (println response)
                                        (cond
                                          (= 200 status) (set! (.-location js/document) (bidi/path-for app-routes :signin))
                                          (or (= 400 status) (= 409 status)) (reset! errors (get-in response [:body :errors])))))}]])))

(defmethod page-contents :signout []
  (unset-login-info)
  (set! (.-location js/document) (bidi/path-for app-routes :index)))

(defmethod page-contents :articles []
  (let [articles (reagent/atom [:div.articles])]
    (fn []
      (when (<= (count @articles) 1)
        (go (let [response (<! (http/get "/v1/articles"))
                  status (:status response)]
              (doseq [article (get-in response [:body :response])]
                (swap! articles conj [:a {:href (bidi/path-for app-routes :article-view :id (:id article))}
                                      [ui/card
                                       [ui/card-header {:title (:title article)
                                                        :subtitle (:user_id article)}]]])))))
      @articles)))

(defmethod page-contents :article-view []
  (let [routing-data (session/get :route)
        item (get-in routing-data [:route-params :id])
        article (reagent/atom [:div.article])]
    (println routing-data)
    (println item)
    (println article)
    (fn []
      (when (<= (count @article) 1)
        (go (let [response (<! (http/get (str "/v1/articles/" item)))
                  status (:status response)
                  body (get-in response [:body :response])]
              (cond
                (= 200 status) (swap! article conj [ui/card 
                                                   [ui/card-header {:title (:title body)
                                                                   :subtitle (:user_id body)}]
                                                   [ui/card-text (:body body)]])
                :else (do (reset! article [:div "Unknown Error"]))))))
      @article)))

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
  (if-let [user-id (:user-id @prefs)]
    [:div 
     [:span user-id]
     [:a {:href (bidi/path-for app-routes :signout)}
      [ui/flat-button "SignOut"]]]
    [:div
     [:a {:href (bidi/path-for app-routes :signin)}
      [ui/flat-button "SignIn"]]
     [:a {:href (bidi/path-for app-routes :signup)}
      [ui/flat-button "SignUp"]]]))

(defn page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [ui/mui-theme-provider
       {:mui-theme (get-mui-theme)}
       [:div
        [ui/app-bar {:title page
                     :iconElementRight (reagent/as-element [right-menu])}] 
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