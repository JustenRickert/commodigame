(ns commgame.loop
  (:require [commgame.state.user :as user]
            [commgame.state.merchant :as merch]))

(defonce ^:private perf-time
  (atom (js/performance.now)))

;; performance.now returns ms, and there are 60 fps.
(defn- time-in-frame []
  (-> (- (js/performance.now) @perf-time)
      (/ 1000)))

(defn- reset-perf-time! []
  (reset! perf-time (js/performance.now)))

(defn- run-user! []
  (user/give-user-money! (* (time-in-frame)
                            (:money-factor @user/state))))

(defn- run-merchant! []
  (let [time-in-frame (time-in-frame)]
    (doseq [[title merch] @merch/state]
      (if (and (> (:quan merch) 0)
              (<= (:time-until merch) 0))
        (do
          (merch/possibly-buy-comm-for-user! (:quan merch) title)
          (merch/reset-time-until! title merch))
        (merch/time-delta-dec! title merch time-in-frame)))))

(defn game! []
  (run-merchant!)
  (run-user!)
  (reset-perf-time!)
  (js/requestAnimationFrame game!))
