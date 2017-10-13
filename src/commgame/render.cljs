(ns commgame.render
  (:require [cljs-time.core :as time]
            [commgame.user :as user]
            [commgame.commodities :as comm]
            [commgame.merchant :as merchant]
            [goog.string :as string]))

(defn comm-li [[title comm]]
  (js/console.log "comm-li" title "rerender")
  [:div.comm title [:br] (:quan comm)])

(defn buy-comm-button [[title comm]]
  (js/console.log "comm-button" title "rerender")
  [:div.comm
   [:div title [:br] "$" (string/format "%.2f" (:price comm))]
   [:button {:on-click #(user/user-buy-one-comm title)}
    "💲"]])

(defn combine-comm-button
  [[title comm]]
  (js/console.log "c-comm-button" title "rerender")
  (let [inputs (:input comm)]
    [:div.comm
     [:span (:output-quan comm) " " title [:br] "@" [:br]
      (for [input inputs]
        (str " " (:quan input) " " (:title input)))]
     [:br]
     [:button {:on-click #(user/user-combine-for-comm title)}
      "🔧"]]))

(defn comb-comm-market []
  ;; (let [titles-with-combs (set (mapv #(:title %) comm/c-comm-data))]
  [:div [:h3 "Type C Commodities Manufacturing"]
   [:div.user-comm
    ;; Need this silly `doall-remove-nil?` wrapper for `for` because reagent
    ;; complains. TODO create a simple macro called `comp-for` or something to
    ;; bypass the issue!
    (let [component (doall
                     (remove nil?
                             (for [[title comm] comm/c-comm-data]
                               (when (user/c-comm-combinable? title)
                                 ^{:key (str "comb btn" title)}
                                 [combine-comm-button [title comm]]))))]
      (if (empty? component)
        [:span "No possible combinations yet!"]
        component))]])

(defn basic-comm-market []
  [:div [:h3 "Type B Commodities Market"]
   [:div.user-comm
    (for [[title comm] comm/b-comm-data]
      ^{:key (str "btn" title)}
      [buy-comm-button [title comm]])]])

(defn user-money [money]
  [:div.money "$" (string/format "%.2f" money)])

(defn user-page []
  [:div.page
   #_[:div.money "$" (string/format "%.2f" )]
   [user-money (:money @user/user-state)]
   [basic-comm-market]
   [comb-comm-market]
   [:div [:h3 "Inventory Commodities"]
    [:div.user-comm
     (let [component (doall
                      (remove nil? (for [[title comm] (:comm @user/user-state)]
                                     (when (not= 0 (:quan comm))
                                       ^{:key (str "li" title)}
                                       [comm-li [title comm]]))))]
       (if (empty? component)
         [:span "No commodities yet!"]
         component))]]])


(defn merchant-li [[title merch]]
  (js/console.log "merchant-li" title "rerender")
  [:div.comm title [:br] (:quan merch)])

(defn merchant-page []
  [:div.page
   [user-money (:money @user/user-state)]
   [:div.user-comm
    (let [component (doall
                     (remove nil? (for [[title merch] @merchant/state]
                                    (when (not= 0 (:quan merch))
                                      ^{:key (str "li" title)}
                                      [merchant-li [title merch]]))))]
      (if (empty? component)
        [:span "No merchants yet!"]
        component))]
   [:div
    [:ul
     (let [now (time/now)]
       (for [[title merch] @merchant/state]
         (let [diff (time/in-seconds (time/interval (:last-time merch)
                                                    now))]
           [:li (str title " " (- (:interval merch) diff))])) )]]
   [:div "What's up?"]])

(defn user-upgrade-page []
  [:div.page
   [:div "What's up?"]])
