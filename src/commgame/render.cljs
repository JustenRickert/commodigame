(ns commgame.render
  (:require [commgame.state.user :as user]
            [commgame.state.data :as data]
            [commgame.state.commodities :as comm]
            [commgame.state.merchant :as merchant]
            [commgame.state.vendor :as vendor]
            [commgame.component.table :as table]
            [goog.string :as string]
            [reagent.core :as r]))

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
                          :comm   [comm-li
                                   (:comm @user/state)
                                   "Inventory Commodities"
                                   "No commodities"]
                          :c-comm [comm-li
                                   (user/c-comm)
                                   "Inventory Combinational Commodities"
                                   "No combinational commodities to sell"]
                          :merch  [merchant-li
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
(defn- c-comm-block-ul [] (block-style-list :c-comm))
(defn- merchant-block-ul [] (block-style-list :merch))

;;; BUY

(defn- buy-button
  [type-key title item]
  (let [[what-type
         what-price
         what-fn] (case type-key
                    :comm  ["commodity"
                            :price
                            user/buy-one-comm!]
                    :merch ["merchant"
                            :merch-price
                            (partial merchant/possibly-buy! 1)]
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
                     :b-comm [data/b-comm
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

;; SELL

(defn- sell-button
  [type-key title item]
  (let [[what-fn] (case type-key
                    :c-comm  [(partial vendor/sell-comm! 1)]
                    (throw (js/Error. "type-key provided matches nothing. Add one?")))]
    [:div.comm
     [:div title [:br] "$" (string/format "%.2f" (:price item))]
     [:button {:on-click #(what-fn title)}
      "💲"]]))

(defn- vendor
  [type-key]
  (let [[data
         h3
         what-key] (case type-key
                     :c-comm [data/c-comm
                              "Type C Commodities Vendor"
                              :c-comm]
                     (throw (js/Error "type-key provided matches nothing. Add one?")))]
    [:div [:h3 h3]
     [:div.user-comm
      (let [component
            (doall
             (remove nil?
                     (for [[title item] data]
                       (let [appropriately-priced-item (get-in @user/state [:comm title])]
                         (when (> (:quan appropriately-priced-item) 0)
                           ^{:key (str "sell-btn" title)}
                           [sell-button what-key title appropriately-priced-item])))))]
        (if (empty? component)
          [:span "No combinational commodities to sell!"]
          component)
        )]]))
(defn c-comm-vendor [] (vendor :c-comm))

;;; TODO COMBINE SECTION

(defn- combine-comm-button
  [[title comm]]
  (let [inputs (:input comm)]
    [:div.comm
     [:span (:output-quan comm) " " title [:br] "@" [:br]
      (for [input inputs]
        (str " " (:quan input) " " (:title input)))]
     [:br]
     [:button {:on-click #(user/user-combine-for-comm! title)}
      "🔧"]]))

(defn- comb-comm-market []
  [:div [:h3 "Type C Commodities Manufacturing"]
   [:div.user-comm
    (let [component (doall
                     (remove nil?
                             (for [[title comm] data/c-comm]
                               (when (user/c-comm-combinable? title)
                                 ^{:key (str "comb btn" title)}
                                 [combine-comm-button [title comm]]))))]
      (if (empty? component)
        [:span "No possible combinations yet!"]
        component))]])

(defn merchant-timers []
  [:ul
   (for [[title merch] @merchant/state]
     (if (> (:quan merch) 0)
       ^{:key (str "timer" title)}
       [:li title " " (string/format "%.1f" (:time-until merch))]))])

(defn user-money []
  [:div.money "$" (string/format "%.2f" (:amount @comm/money))])

;;; RENDER PAGE

(defn user-page []
  [:div.page
   [user-money]
   [table/b-comm]
   [table/c-comm]])

(defn processor-page []
  [:div.page
   [user-money]
   #_[table/processor]
   #_[merchant-block-ul]
   #_[merchant-market]
   #_[merchant-timers]])

(defn vendor-page []
  [:div.page
   [user-money]
   [c-comm-block-ul]
   [c-comm-vendor]])

(defn manufacturing-page []
  [:div.page
   [user-money]
   #_[table/manufacturers]])

(defn upgrade-page []
  [:div.page
   [:div "What's up?"]])

