(ns commgame.merchant
  (:require [reagent.core :as r]

            [commgame.commodities :as comm]
            [commgame.user :as user]
            [cljs-time.core :as time]))

#_(defonce timer-state
    (atom (into {}
                (for [[k _] comm/b-comm-data]
                  {k {:last-time (time/now)}}))))

(defonce perf-time (atom (js/performance.now)))
(defonce state
  (r/atom (into {}
                (for [[title comm] comm/b-comm-data]
                  {title {:merch-price   (:merch-price comm)
                          :time-until    0
                          :time-factor   1
                          :time-interval 5
                          :quan          0}}))))

;;performance.now returns ms, and there are 60 fps
(defn time-between-frames []
  (-> (- (js/performance.now) @perf-time)
      (/ 1000)))

(defn time-delta-dec! [title merch]
  (swap! state update-in [title :time-until] #(- % (* (:time-factor merch)
                                                      (time-between-frames)))))

(defn reset-time-until! [title merch]
  (swap! state assoc-in [title :time-until] (:time-interval merch)))

(defn swap-perf-time! []
  (reset! perf-time (js/performance.now)))

(defn possibly-purchase! [amt title]
  (let [cost (* amt (get-in @state [title :merch-price]))]
    (if (>= (:money @user/state) cost)
      (do
        (user/take-user-money! cost)
        (add! amt title) )
      (.log js/console "Cannot purchase" amt title "merchant"))))

(defn add! [amt title]
  (swap! state update-in [title :quan] #(+ % amt)))

(defn remove! [title amt]
  (swap! state update-in [title :quan] #(- % amt)))

(defn possibly-buy-comm-for-user! [title amt]
  (let [user-money (get @user/state :money)
        comm       (get-in @user/state [:comm title])]
    (when (> user-money (* amt (:price comm)))
      (user/give-user-comm! title amt)
      (user/take-user-money! (* amt (:price comm))))))

(defn timer-loop! []
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
