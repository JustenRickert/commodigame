(ns commgame.loop
  (:require [commgame.state.commodities :as comm]
            [commgame.state.user :as user]
            [commgame.state.merchant :as merch]
            [commgame.state.manufacturer :as manufacturer]))

(defonce ^:private perf-time
  (atom (js/performance.now)))

;; performance.now returns ms, and there are 60 fps.
(defn- time-in-frame []
  (-> (- (js/performance.now) @perf-time)
      (/ 1000)))

(defn- reset-perf-time! []
  (reset! perf-time (js/performance.now)))

#(defn- run-user! []
  (user/give-user-money! (* (time-in-frame) (:money-factor @user/state))))

(defn- run-merchant! []
  (let [time-in-frame (time-in-frame)]
    (doseq [[title merch] @merch/state]
      (if (and (> (:quan merch) 0)
              (<= (:time-until merch) 0))
        (do
          (merch/possibly-buy-comm-for-user! (:quan merch) title)
          (merch/reset-time-until! title merch))
        (merch/time-delta-dec! title merch time-in-frame)))))

(defn- run-manufacturer! []
  (let [time-in-frame (time-in-frame)]
    (doseq [[title man] @manufacturer/state]
      (if (and (> (:quan man) 0)
               (<= (:time-until man) 0))
        (do
          (manufacturer/possibly-manufacture! (:quan man) title)
          (manufacturer/reset-time-until! title man))
        (manufacturer/time-delta-dec! title man time-in-frame)))))

(defn game! []
  (comm/inc-money! {:time (time-in-frame)})
  ;; (run-merchant!)
  ;; (run-manufacturer!)
  ;; (run-user!)
  (reset-perf-time!)
  (js/requestAnimationFrame game!))
