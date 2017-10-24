(ns commgame.state.manufacturer
  (:require [commgame.state.user :as user]
            [commgame.state.data :as data]
            [reagent.core :as r]))

(defonce state
  (r/atom (into {}
                (for [[title comm] data/c-comm]
                  {title {:man-price     (:man-price comm)
                          :time-until    0
                          :time-factor   1
                          :time-interval 5
                          :quan          0}}))))

(defn reset-time-until! [title man]
  (swap! state assoc-in [title :time-until] (:time-interval man)))

(defn time-delta-dec!
  [title man time-in-frame]
  (swap! state update-in [title :time-until] #(- % (* time-in-frame
                                                      (:time-factor man)))))

(defn add! [amt title]
  (swap! state update-in [title :quan] #(+ % amt)))

(defn possibly-manufacture-one! [title]
  (if (user/c-comm-combinable? title)
    (user/user-combine-for-comm! title)))

(defn possibly-manufacture! [amt title]
  (dotimes [_ amt] (possibly-manufacture-one! title)))
