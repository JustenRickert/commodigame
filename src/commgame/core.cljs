(ns commgame.core
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require [clojure.string :as string]
            [reagent.core :as r :refer [atom]]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary]

            [commgame.loop :as loop]
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
  (defroute "/user" [] (swap! app-state assoc :page :user))
  (defroute "/processor" [] (swap! app-state assoc :page :processor))
  (defroute "/vendor" [] (swap! app-state assoc :page :vendor))
  (defroute "/manufacturing" [] (swap! app-state assoc :page :manufacturing))
  (defroute "/upgrade" [] (swap! app-state assoc :page :upgrade))
  (hook-browser-navigation!))

(def href-data
  [{:route "#/welcome" :title "Welcome"}
   {:route "#/user" :title "User"}
   {:route "#/processor" :title "Processor"}
   {:route "#/manufacturing" :title "Manufacturing"}
   {:route "#/vendor" :title "Vendor"}
   {:route "#/upgrade" :title "Upgrades"}])

(defn- href-datum-to-a [d]
  ^{:key (string/lower-case (:title d))}
  [:a {:href (:route d)} (:title d) [:br]])

(defn- list-all-href-but [current-title]
  [:div (->> href-data
             (filter #(not= (:title %) current-title))
             (map href-datum-to-a))])

(defn processor []
  [:div
   [:div.sidebar
    [list-all-href-but "Processor"]]
   [:div.content
    [:h1 "Processor"]
    [render/processor-page]]])

(defn manufacturing []
  [:div
   [:div.sidebar
    [list-all-href-but "Manufacturing"]]
   [:div.content
    [:h1 "Manufacturing"]
    [render/manufacturing-page]]])

(defn vendor []
  [:div
   [:div.sidebar
    [list-all-href-but "Vendor"]]
   [:div.content
    [:h1 "Vendor"]
    [render/vendor-page]]])

(defn upgrade []
  [:div
   [:div.sidebar
    [list-all-href-but "Upgrades"]]
   [:div.content
    [:h1 "Upgrades"]
    [render/upgrade-page]]])

(defn user []
  [:div
   [:div.sidebar
    [list-all-href-but "User"]]
   [:div.content
    [:h1 "User"]
    [render/user-page]]])

(defn welcome []
  [:div
   [:div.sidebar
    [list-all-href-but "Welcome"]]
   [:div.content
    [:h1 "Welcome!"]
    [:p "Here are the basics:"]
    [:ol
     [:li "Purchase basic commodities (type B commodities) as yourself (the user)"]
     [:li "Combine basic commodities into combinational commodities (type C commodities)"]
     [:li "Sell the commodities (as a vendor)"]]
    [:p "When you've made enough money doing the above, you can attain employees to do the tedious work for you. Then (hopefully) you'll have you some great fun!"]]])

(defmulti current-page #(@app-state :page))
(defmethod current-page :default [] [welcome])
(defmethod current-page :user [] [user])
(defmethod current-page :processor [] [processor])
(defmethod current-page :manufacturing [] [manufacturing])
(defmethod current-page :vendor [] [vendor])
(defmethod current-page :welcome [] [welcome])
(defmethod current-page :upgrade [] [upgrade])

(app-routes)
(r/render-component [current-page]
                    (. js/document (getElementById "app")))
(loop/game!)

(defn on-js-reload
  []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  #_(swap! app-state update-in [:__figwheel_counter] inc))
