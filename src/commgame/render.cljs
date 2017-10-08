(ns commgame.render
  (:require [commgame.core :as game]
            [commgame.commodities :as comm]
            [goog.string :as string]))

(defn comm-li
  [comm]
  (js/console.log "comm-li rerender")
  [:div.comm
   [:div (:title comm) [:br] (:quan comm)]])

(defn buy-comm-button
  [comm]
  (js/console.log "buy-comm-button rerender")
  ;; (js/console.log (comm/comm-titles-with-combs ))
  [:div.comm
   [:div (:title comm) [:br] "$" (string/format "%.2f" (:price comm))]
   [:button {:on-click #(game/user-buy-one-comm (:title comm))}
    "💲"]])

(defn combine-comm-button
  [comm]
  (js/console.log "buy-comm-comb-button rerender")
  (let [inputs (comm/get-comb-inputs-by-title (:title comm))
        output (comm/get-comb-output-by-title (:title comm))]
    [:div.comm
     [:span (:quan output) " " (:title output) [:br] "@" [:br]
      (for [input inputs]
        (str " " (:quan input) " " (:title input)))]
     [:br]
     [:button {:on-click #(game/user-combine-for-comm (:title comm))}
      "🔧"]]))

(defn comm-comb-market []
  (let [titles-with-combs (comm/comm-titles-with-combs)]
    [:div [:h3 "Combination"]
     [:div.user-comm
     ;; Need remove nil? because the when fn returns nil and need doall because
     ;; reagent complains about evaluating lazy sequences. FIXME ?
      (let [comp (doall
                  (remove nil? (for [comm (game/user-commodities)]
                                 (when (and
                                        (contains? titles-with-combs (:title comm))
                                        (game/user-can-combine-for-comm (:title comm)))
                                   [combine-comm-button comm]))))]
        (if (empty? comp)
          [:span "No possible combinations yet!"]
          comp))]]))

(defn basic-comm-market []
  (let [titles-with-combs (comm/comm-titles-with-combs)]
    [:div [:h3 "Market"]
     [:div.user-comm
      (for [comm (game/user-commodities)]
        (when (not (contains? titles-with-combs (:title comm)))
          [buy-comm-button comm]))]]))

(defn page []
  [:div.page
   [:div.money "$" (string/format "%.2f" (get-in @game/app-state [:user :money]))]
   [basic-comm-market]
   [comm-comb-market]
   [:div [:h3 "User commodities"]
    [:div.user-comm
     (let [comp (doall (remove nil? (for [c (game/user-commodities)]
                  (when (not= 0 (:quan c))
                    [comm-li c])) ))]
       (if (empty? comp)
         [:span "No commodities yet!"]
         comp))]]])
