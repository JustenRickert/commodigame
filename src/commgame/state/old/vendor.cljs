(ns commgame.state.vendor
  (:require [reagent.core :as r]
            [cljs-time.core :as time]

            [commgame.state.data :as data]
            [commgame.state.user :as user]))

(defonce perf-time (atom (js/performance.now)))
(defonce state
  (r/atom (into {}
                (for [[title comm] data/c-comm]
                  {title {:vendor-price  (:vendor-price comm)
                          :time-until    0
                          :time-factor   1
                          :time-interval 5
                          :quan          0}}))))

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

(defn sell-comm! [amt title]
  (let [comm (get-in @user/state [:comm title])]
    (if (>= (:quan comm) amt)
      (do
        (user/take-user-comm! title amt)
        (user/give-user-money! (* amt (:price comm))))
      (.log js/console "Can't sell" amt title))))

(defn possibly-sell-one-comm! [title]
  (sell-comm! 1 title))

(defn add! [amt title]
  (swap! state update-in [title :quan] #(+ % amt)))

(defn remove! [title amt]
  (swap! state update-in [title :quan] #(- % amt)))

(defn possibly-purchase! [amt title]
  (let [cost (* amt (get-in @state [title :vendor-price]))]
    (if (>= (:money @user/state) cost)
      (do
        (user/take-user-money! cost)
        (add! amt title))
      (.log js/console "Cannot purchase" amt title "vendor"))))

(defn timer-loop! []
  (let [now (time/now)]
    (doseq [[title merch] @state]
      (if (and (> (:quan merch) 0)
               (<= (:time-until merch) 0))
        (do
          (dotimes [_ (get-in @state [title :quan])]
            (possibly-sell-one-comm! title) )
          (reset-time-until! title merch))
        (time-delta-dec! title merch))))
  (swap-perf-time!)
  (js/requestAnimationFrame timer-loop!))

