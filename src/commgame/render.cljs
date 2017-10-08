(ns commgame.render
  (:require [commgame.core :as game]
            [commgame.commodities :as comm]
            [goog.string :as string]))

(defn comm-li
  [comm]
  (js/console.log "comm-li rerender")
  [:li
   [:p "comm: " (:title comm)]
   [:p "quan: " (:quan comm)]])

(defn buy-comm-button
  [comm]
  (js/console.log "buy-comm-button rerender")
  ;; (js/console.log (comm/comm-titles-with-combs ))
  [:p
   [:span (:title comm) " @ $" (string/format "%.2f" (:price comm))]
   [:button {:on-click #(game/user-buy-one-comm (:title comm))}
    "Buy"]])

(defn combine-comm-button
  [comm]
  (js/console.log "buy-comm-comb-button rerender")
  (let [inputs (comm/get-comb-inputs-by-title (:title comm))
        output (comm/get-comb-output-by-title (:title comm))]
    [:p
     [:span (:quan output) " " (:title output) " @"
      (for [input inputs]
        (str " " (:quan input) " " (:title input)))]
     [:button {:on-click #(game/user-combine-for-comm (:title comm))}
      "Combine"]]))

(defn comm-comb-market []
  (let [titles-with-combs (comm/comm-titles-with-combs)]
    [:div
     ;; Need remove nil? because the when fn returns nil and need doall because
     ;; reagent complains about evaluating lazy sequences. FIXME ?
     (doall (remove nil? (for [comm (game/user-commodities)]
                           (when (and
                                  (contains? titles-with-combs (:title comm))
                                  (game/user-can-combine-for-comm (:title comm)))
                             [combine-comm-button comm]))))]))

(defn basic-comm-market []
  (let [titles-with-combs (comm/comm-titles-with-combs)]
    [:div
     (for [comm (game/user-commodities)]
       (when (not (contains? titles-with-combs (:title comm)))
         [buy-comm-button comm]))]))

(defn page []
  [:div
   [basic-comm-market]
   [comm-comb-market]
   [:p "User money: " (string/format "%.2f" (get-in @game/app-state [:user :money]))]
   [:ul
    (for [c (game/user-commodities)]
      (when (not= 0 (:quan c))
        [comm-li c]))]])
