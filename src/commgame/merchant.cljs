(ns commgame.merchant
  (:require [commgame.commodities :as comm]
            [cljs-time.core :as time]))

#_(defonce timer-state
  (atom (into {}
              (for [[k _] comm/b-comm-data]
                {k {:last-time (time/now)}}))))
(defonce state
  (atom (into {}
              (for [[k _] comm/b-comm-data]
                {k {:last-time (time/now)
                    :interval 5
                    :quan 0}}))))

(defn swap-last-time! [title]
  (swap! state update-in [title :last-time] #(time/now)))

(defn add-merchant! [title amt]
  (swap! state update-in [title :quan] #(+ % amt)))

(defn remove-merchant! [title amt]
  (swap! state update-in [title :quan] #(- % amt)))

#_(defn fireable? [title amt])

(time/in-seconds (time/interval (time/date-time 2017 10 13)
                                (time/now)))

(defn timer-loop! []
  (let [now (time/now)]
    (doseq [[title time-then] @state]
      (let [diff (time/in-seconds (time/interval (:last-time time-then)
                                                 now))]
        (when (>= diff
                  (get-in @state [title :interval]))
          (do                           ;TODO need for merchants to purchase commodities
            (swap-last-time! title))))))
  (js/requestAnimationFrame timer-loop!))