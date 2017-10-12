(ns commgame.render
  (:require [commgame.game :as game]
            [commgame.commodities :as comm]
            [goog.string :as string]))

(defn comm-li [[title comm]]
  (js/console.log "comm-li" title "rerender")
  [:div.comm title [:br] (:quan comm)])

(defn buy-comm-button [[title comm]]
  (js/console.log "comm-button" title "rerender")
  [:div.comm
   [:div title [:br] "$" (string/format "%.2f" (:price comm))]
   [:button {:on-click #(game/user-buy-one-comm title)}
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
     [:button {:on-click #(game/user-combine-for-comm title)}
      "🔧"]]))

(defn comb-comm-market []
  ;; (let [titles-with-combs (set (mapv #(:title %) comm/c-comm-data))]
  [:div [:h3 "Combination"]
   [:div.user-comm
    (let [component (doall
                     (remove nil?
                             (for [[title comm] comm/c-comm-data]
                               (when (game/c-comm-combinable? title)
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

(defn page []
  [:div.page
   [:div.money "$" (string/format "%.2f" (:money @game/user-state))]
   [basic-comm-market]
   [comb-comm-market]
   [:div [:h3 "User commodities"]
    [:div.user-comm
     (let [component (doall
                      (remove nil? (for [[title comm] (:comm @game/user-state)]
                                     (when (not= 0 (:quan comm))
                                       ^{:key (str "li" title)}
                                       [comm-li [title comm]]))))]
       (if (empty? component)
         [:span "No commodities yet!"]
         component))]]])
