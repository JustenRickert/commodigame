(ns commgame.loop
  (:require [commgame.state.commodities :as comm]
            [commgame.state.employees :as employ]))

(defonce ^:private perf-time
  (atom (js/performance.now)))

;; performance.now returns ms, and there are 60 fps.
(defn- time-in-frame []
  (-> (- (js/performance.now) @perf-time)
      (/ 1000)))

(defn- reset-perf-time! []
  (reset! perf-time (js/performance.now)))

(defn- run-processor! []
  (let [time-in-frame (time-in-frame)]
    (doseq [[title proc] employ/processors-time]
      (if (and (> (-> employ/processors (get title) deref :quan) 0)
               (<= (:time-until @proc) 0))
        (do
          (employ/reset-time-until! (:type @proc) title)
          (comm/possibly-buy! :b-comm
                              title
                              (min (comm/max-buyable :b-comm title)
                                   (-> employ/processors (get title) deref :quan))))
        (do
          (employ/dec-time! {:time  time-in-frame
                             :type  (:type @proc)
                             :title title}))))))

(defn- run-manufacturer! []
  (let [time-in-frame (time-in-frame)]
    (doseq [[title proc] employ/manufacturers-time]
      (if (and (> (-> employ/manufacturers (get title) deref :quan) 0)
               (<= (:time-until @proc) 0))
        (do
          (employ/reset-time-until! (:type @proc) title)
          (dotimes [_ (-> employ/manufacturers (get title) deref :quan)]
            (comm/possibly-combine! title)))
        (do
          (employ/dec-time! {:time  time-in-frame
                             :type  (:type @proc)
                             :title title}))))))

(defn- run-vendor!
  "TODO need possibly-sell! and things first"
  []
  (let [time-in-frame (time-in-frame)]
    (doseq [[title proc] employ/vendors-time]
      (if (and (> (-> employ/vendors (get title) deref :quan) 0)
               (<= (:time-until @proc) 0))
        (do
          (employ/reset-time-until! (:type @proc) title)
          (comm/possibly-sell! :c-comm
                               title
                               (min (comm/max-sellable :c-comm title)
                                    (-> employ/vendors (get title) deref :quan))))
        (do
          (employ/dec-time! {:time  time-in-frame
                             :type  (:type @proc)
                             :title title}))))))

(defn game! []
  (comm/inc-money! {:time (time-in-frame)})
  (run-processor!)
  (run-manufacturer!)
  (run-vendor!)
  (reset-perf-time!)
  (js/requestAnimationFrame game!))
