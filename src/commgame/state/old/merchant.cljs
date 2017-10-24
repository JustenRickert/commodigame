(ns commgame.state.merchant
  (:require [reagent.core :as r]
            [cljs-time.core :as time]

            [commgame.state.data :as data]
            [commgame.state.user :as user]))

(defonce state
  (r/atom (into {}
                (for [[title comm] data/b-comm]
                  {title {:merch-price   (:merch-price comm)
                          :time-until    0
                          :time-factor   1
                          :time-interval 5
                          :quan          0}}))))

(defn time-delta-dec!
  [title merch time-in-frame]
  (swap! state update-in [title :time-until] #(- % (* time-in-frame
                                                      (:time-factor merch)))))

(defn reset-time-until! [title merch]
  (swap! state assoc-in [title :time-until] (:time-interval merch)))

(defn add! [amt title]
  (swap! state update-in [title :quan] #(+ % amt)))

(defn remove! [title amt]
  (swap! state update-in [title :quan] #(- % amt)))

(defn amount-buyable
  "TODO Returns maximum amount of buyable merchants with given title"
  [title]
  nil)

(defn buyable?
  "Returns if user can buy the amount of merchants with given title"
  [amt title]
  (let [cost (* amt (get-in @state [title :merch-price]))]
    (>= (:money @user/state) cost)))

(defn buy!
  "Buys the amount of merchants with given title"
  [amt title]
  (let [cost (* amt (get-in @state [title :merch-price]))]
    (do
      (user/take-user-money! cost)
      (add! amt title))))

(defn possibly-buy!
  "Possibly buys the amount of merchants with given title if the cost is within
  user money total"
  [amt title]
  (let [cost (* amt (get-in @state [title :merch-price]))]
    (if (>= (:money @user/state) cost)
      (do
        (user/take-user-money! cost)
        (add! amt title))
      (.log js/console "Cannot purchase" amt title "merchant"))))

;;; Buying comms doen't necessarily require the merchant state data, but it
;;; makes sense for these state mutations to be in this file.

(defn amount-comm-buyable
  "TODO Returns the total amount of commodities buyable by user"
  [title])

(defn buy-least-possible-comm!
  "TODO Buys the least amount of commodities with given title possible"
  [title])

(defn possibly-buy-comm-for-user!
  "Possibly buys the amount of commodities with given title if the cost is
  within user money total"
  [amt title]
  (let [user-money (get @user/state :money)
        comm       (get-in @user/state [:comm title])]
    (when (> user-money (* amt (:price comm)))
      (user/give-user-comm! title amt)
      (user/take-user-money! (* amt (:price comm))))))

#_(defn timer-loop! []
    (let [now (time/now)]
      (doseq [[title merch] @state]
        (if (and (> (:quan merch) 0)
                (<= (:time-until merch) 0))
          (do
            (possibly-buy-comm-for-user! title (:quan merch))
            (reset-time-until! title merch))
          (time-delta-dec! title merch))))
    (swap-perf-time!)
    (js/requestAnimationFrame timer-loop!))
