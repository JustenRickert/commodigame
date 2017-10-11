(ns commgame.game
  (:require [commgame.commodities :as comm]

            [cljs-time.core :as time]
            [cljs-time.format :as time-format]
            [goog.string :as string]
            [reagent.core :as reagent :refer [atom]]))

(def time-formatter (time-format/formatter "yyyy/MM/dd hh:mm:ss UTC"))

(defn add-zero-quantity [item] (assoc item :quan 0))

;; Note that atom is a reagent atom here
(defonce app-state
  (atom {:begin-time (time/now)
         :time-now (time/now)
         :time-last-tick (time/now)

         :user {:money 0
                :money-delta 0.1
                :commodities (mapv #(-> % add-zero-quantity)
                                   (concat comm/b-comm-data comm/c-comm-data))}}))

#_(defn seconds-since-begin-time []
    (time/in-seconds (time/interval (:begin-time @app-state) (time/now))))

#_(defn seconds-since-last-tick []
    (time/in-seconds (time/interval (:time-last-tick @app-state) (time/now))))

(defn user-commodities []
  (get-in @app-state [:user :commodities]))

(defn give-user-money! [amt]
  (swap! app-state update-in [:user :money]
         #(+ (get-in @app-state [:user :money]) amt)))

(defn take-user-money! [amt]
  (swap! app-state update-in [:user :money]
         #(- (get-in @app-state [:user :money]) amt)))

(defn user-comm-of-title [title]
  (loop [comm (get-in @app-state [:user :commodities])]
    (cond (= title (:title (first comm))) (first comm)
          (empty? comm) nil
          :else (recur (rest comm)))))

(defn give-user-comm! [title amt]
  (let [user-comm (user-comm-of-title title)]
    (swap! app-state update-in [:user :commodities]
           (fn [] (mapv #(if (= (:title %) title)
                           (assoc % :quan (+ (:quan user-comm) amt))
                           (identity %))
                        (get-in @app-state [:user :commodities]))))))

(defn take-user-comm! [title amt]
  (let [user-comm (user-comm-of-title title)]
    (swap! app-state update-in [:user :commodities]
           (fn [] (mapv #(if (= (:title %) title)
                           (assoc % :quan (- (:quan user-comm) amt))
                           (identity %))
                        (get-in @app-state [:user :commodities]))))))

(defn timer-loop! []
  (do
    ;; increment user money
    (let [user (:user @app-state)]
      (swap! app-state update-in [:user :money]
             #(+ (:money user) (:money-delta user))))
    ;; increment things to display the time
    (swap! app-state update-in [:time-last-tick] time/now)
    (swap! app-state update-in [:time-now] time/now))
  (js/requestAnimationFrame timer-loop!))

(defn format-time [time-key]
  (time-format/unparse time-formatter (time-key @app-state)))

(defn user-buy-one-comm [title]
  (let [comm-buying (user-comm-of-title title)]
    (if (> (get-in @app-state [:user :money]) (:price comm-buying))
      (do
        (take-user-money! (:price comm-buying))
        (give-user-comm! (:title comm-buying) 1))
      (js/console.log "Not enough money to buy" title "!"))))

(defn c-comm-combinable? [title]
  (let [comb-inputs (comm/comb-inputs-of-title title)
        user-comms (user-commodities)]
    (loop [input comb-inputs]
      (cond
        (< (:quan (user-comm-of-title (:title (first input))))
           (:quan (first input))) false
        (empty? input) true
        :else (recur (rest input))))))

(defn user-combine-for-comm [c-comm]
  (doseq [input (:input c-comm)]
    (take-user-comm! (:title input) (:quan input)))
  (give-user-comm! (:title c-comm) (:output-quan c-comm)))
