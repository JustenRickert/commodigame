(ns commgame.render
  (:require [commgame.game :as game]
            [commgame.commodities :as comm]
            [goog.string :as string]))

(defn comm-li
  [comm]
  (js/console.log "comm-li rerender")
  [:div.comm
   [:div (:title comm) [:br] (:quan comm)]])

(defn buy-comm-button [comm]
  (js/console.log "buy-comm-button rerender")
  [:div.comm
   [:div (:title comm) [:br] "$" (string/format "%.2f" (:price comm))]
   [:button {:on-click #(game/user-buy-one-comm (:title comm))}
    "💲"]])

(defn combine-comm-button
  [c-comm]
  (js/console.log "c-comm-button rerender")
  (let [inputs (comm/comb-inputs-of-title (:title c-comm))]
    [:div.comm
     [:span (:output-quan c-comm) " " (:title c-comm) [:br] "@" [:br]
      (for [input inputs]
        (str " " (:quan input) " " (:title input)))]
     [:br]
     [:button {:on-click #(game/user-combine-for-comm c-comm)}
      "🔧"]]))

(defn comb-comm-market []
  ;; (let [titles-with-combs (set (mapv #(:title %) comm/c-comm-data))]
  [:div [:h3 "Combination"]
   [:div.user-comm
      ;; comp is component. A react component.
    (let [comp (doall (remove nil? (for [c-comm comm/c-comm-data]
                                     (when (game/c-comm-combinable? (:title c-comm))
                                       [combine-comm-button c-comm]))))]
      (if (empty? comp)
        [:span "No possible combinations yet!"]
        comp))]])

(defn basic-comm-market []
  [:div [:h3 "Type B Commodities Market"]
   [:div.user-comm
    (for [comm comm/b-comm-data]
      [buy-comm-button comm])]])

(defn page []
  [:div.page
   [:div.money "$" (string/format "%.2f" (get-in @game/app-state [:user :money]))]
   [basic-comm-market]
   [comb-comm-market]
   [:div [:h3 "User commodities"]
    [:div.user-comm
     (let [comp (doall (remove nil? (for [c (game/user-commodities)]
                                      (when (not= 0 (:quan c))
                                        [comm-li c]))))]
       (if (empty? comp)
         [:span "No commodities yet!"]
         comp))]]])
