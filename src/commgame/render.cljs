(ns commgame.render
  (:require [cljs-time.core :as time]
            [commgame.user :as user]
            [commgame.commodities :as comm]
            [commgame.merchant :as merchant]
            [goog.string :as string]))

;;; HELPER

(defn pluralize [word]
  (case word
    "commodity" "commodities"
    "merchant" "merchants"
    ""))

;;; LIST

(defn- list-item
  [type-key title item]
  (let [what-type (case type-key
                    :comm  ""
                    :merch "merchant"
                    (throw  (js/Error. "type-key provided matches nothing. Add one?")))
        quan      (:quan item)]
    [:div.comm title " " (if (> quan 1) (pluralize what-type) what-type)
     [:br] quan]))

(defn- comm-li [title comm] (list-item :comm title comm))
(defn- merchant-li [title merch] (list-item :merch title merch))

(defn- block-style-list
  [type-key]
  (let [[what-li
         what-data
         h3
         empty-message] (case type-key
                          :comm  [comm-li
                                  (:comm @user/state)
                                  "Inventory Commodities"
                                  "No commodities"]
                          :merch [merchant-li
                                  @merchant/state
                                  "Employed Merchants"
                                  "No employee merchants"]
                          (throw (js/Error. "type-key provided matches nothing. Add one?")))]
    [:div [:h3 h3]
     [:div.user-comm
      (let [component (doall
                       (remove nil? (for [[title item] what-data]
                                      (when (not= 0 (:quan item))
                                        ^{:key (str "li" title)}
                                        [what-li title item]))))]
        (if (empty? component)
          [:span empty-message]
          component))]]))

(defn- comm-block-ul [] (block-style-list :comm))
(defn- merchant-block-ul [] (block-style-list :merch))

;;; BUY

(defn- buy-button
  [type-key title item]
  (let [[what-type
         what-price
         what-fn] (case type-key
                    :comm  ["commodity"
                            :price
                            user/user-buy-one-comm]
                    :merch ["merchant"
                            :merch-price
                            (partial merchant/add! 1)]
                    (throw (js/Error. "type-key provided matches nothing. Add one?")))]
    [:div.comm
     [:div title [:br] "$" (string/format "%.2f" (what-price item))]
     [:button {:on-click #(what-fn title)}
      "💲"]]))

(defn- buy-comm-button [[title comm]] (buy-button :comm title comm))
(defn- buy-merchant-button [[title merch]] (buy-button :merch title merch))

(defn- market
  [type-key]
  (let [[data
         h3
         what-key] (case type-key
                     :b-comm [comm/b-comm-data
                              "Type B Commodities Market"
                              :comm]
                     :merch  [@merchant/state
                              "Merchant Market"
                              :merch]
                     (throw (js/Error "type-key provided matches nothing. Add one?")))]
    [:div [:h3 h3]
     [:div.user-comm
      (for [[title item] data]
        ^{:key (str "btn" title)}
        [buy-button what-key title item])]]))

(defn- b-comm-market [] (market :b-comm))
(defn- merchant-market [] (market :merch))

;;; TODO COMBINE SECTION

(defn- combine-comm-button
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

(defn- comb-comm-market []
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

(defn- user-money [money]
  [:div.money "$" (string/format "%.2f" money)])

;;; RENDER PAGE

(defn user-page []
  [:div.page
   [user-money (:money @user/state)]
   [comm-block-ul]
   [b-comm-market]
   [comb-comm-market]])

(defn merchant-page []
  [:div.page
   [user-money (:money @user/state)]
   [merchant-block-ul]
   [merchant-market]
   [:div
    [:ul
     (let [now (time/now)]
       (for [[title merch] @merchant/state]
         (let [diff (time/in-seconds (time/interval (:last-time merch)
                                                    now))]
           ^{:key (str "li-merch" title)}
           [:li (str title " " (- (:interval merch) diff))])))]]
   [:div "What's up?"]])

(defn user-upgrade-page []
  [:div.page
   [:div "What's up?"]])
