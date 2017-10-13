(ns commgame.core
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary]

            [commgame.commodities :as comm]
            [commgame.user :as user]
            [commgame.merchant :as merchant]
            [commgame.render :as render]))

(enable-console-print!)

(def app-state (atom {}))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  (defroute "/" [] (swap! app-state assoc :page :welcome))
  (defroute "/welcome" [] (swap! app-state assoc :page :welcome))
  (defroute "/merchant" [] (swap! app-state assoc :page :merchant))
  (defroute "/user" [] (swap! app-state assoc :page :user))
  (defroute "/upgrade" [] (swap! app-state assoc :page :upgrade))
  (hook-browser-navigation!))

;; TODO Make a programmatic way to make a lists of links, filtering out the
;; current page. Probably not required, but it's an easy improvement to learn
;; from.

(defn merchant []
  [:div
   [:div.sidebar
    [:a {:href "#/welcome"} "Welcome"][:br]
    [:a {:href "#/user"} "User"] [:br]
    [:a {:href "#/upgrade"} "Upgrades"]]
   [:div.content
    [:h1 "Merchant"]
    [render/merchant-page]]])

(defn upgrade []
  [:div
   [:div.sidebar
    [:a {:href "#/welcome"} "Welcome"][:br]
    [:a {:href "#/merchant"} "Merchant"][:br]
    [:a {:href "#/user"} "User"]]
   [:div.content
    [:h1 "Upgrades"]
    [render/user-upgrade-page]]])

(defn welcome []
  [:div
   [:div.sidebar
    [:a {:href "#/merchant"} "Merchant"] [:br]
    [:a {:href "#/user"} "User"] [:br]
    [:a {:href "#/upgrade"} "Upgrades"] [:br]]
   [:div.content
    [:h1 "Welcome!"]]])

(defn user []
  [:div
   [:div.sidebar
    [:a {:href "#/welcome"} "Welcome"] [:br]
    [:a {:href "#/merchant"} "Merchant"] [:br]
    [:a {:href "#/upgrade"} "Upgrades"]]
   [:div.content
    [:h1 "User"]
    [render/user-page]]])

(defmulti current-page #(@app-state :page))
(defmethod current-page :default []
  [welcome])
(defmethod current-page :upgrade []
  [upgrade])
(defmethod current-page :merchant []
  [merchant])
(defmethod current-page :welcome []
  [welcome])
(defmethod current-page :user []
  [user])


(app-routes)
(reagent/render-component [current-page]
                          (. js/document (getElementById "app")))
(user/timer-loop!)
(merchant/timer-loop!)

#_(defn on-js-reload
  []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
)
