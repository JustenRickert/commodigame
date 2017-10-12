(ns commgame.game
  (:require [commgame.commodities :as comm]

            [cljs-time.core :as time]
            [cljs-time.format :as time-format]
            [goog.string :as string]
            [reagent.core :as reagent :refer [atom]]))

(defn add-zero-quantity [item] (assoc item :quan 0))

(defonce time-state
  (atom {:begin-time (time/now)
         :time-last-tick (time/now)} ))

;; Note that atom is a reagent atom here.
(defonce user-state
  (atom {:money       0
         :money-delta 0.1
         :comm (into {}
                     (for [[k v] (merge comm/b-comm-data comm/c-comm-data)]
                       {k (assoc v :quan 0)}))}))

(def time-formatter (time-format/formatter "yyyy/MM/dd hh:mm:ss UTC"))
(defn format-time [time-key]
  (time-format/unparse time-formatter (time-key @time-state)))

#_(defn seconds-since-begin-time []
    (time/in-seconds (time/interval (:begin-time @app-state) (time/now))))

#_(defn seconds-since-last-tick []
    (time/in-seconds (time/interval (:time-last-tick @app-state) (time/now))))

;; `!` denotes state mutation.
(defn give-user-money! [amt]
  (swap! user-state update :money #(+ (:money @user-state) amt)))

(defn take-user-money! [amt]
  (swap! user-state update :money #(- (:money @user-state) amt)))

;; TODO Most of these functions with `title` parameter could probably be made
;; into multimethods taking either a `comm` or a `title`. Some of the names
;; would need to be changed also. I think it's not idiomatic Clojure to use such
;; lengthy titles (which would be elided with multimethods).

(defn give-user-comm! [title amt]
  (swap! user-state update-in [:comm title :quan] #(+ % amt)))

(defn take-user-comm! [title amt]
  (swap! user-state update-in [:comm title :quan] #(- % amt)))

;; TODO This should use time differences in calculations to account for low
;; frame rate and tab halt/close situations. Shouldn't be difficult at all.

(defn timer-loop! []
  (do
    ;; increment user money
    (let [user @user-state]
      (swap! user-state update :money #(+ (:money user) (:money-delta user))))
    ;; increment things to display the time
    (swap! time-state update :time-last-tick time/now)
    (swap! time-state update :time-now time/now))
  (js/requestAnimationFrame timer-loop!))

(defn user-buy-one-comm [title]
  (let [comm-buying (get (:comm @user-state) title)]
    (if (> (:money @user-state) (:price comm-buying))
      (do
        (js/console.log "Buying" title)
        (take-user-money! (:price comm-buying))
        (give-user-comm! title 1))
      (js/console.log "Not enough money to buy" title "!"))))

(defn c-comm-combinable? [title]
  (let [inputs (get-in (:comm @user-state) [title :input])
        user-comms (:comm @user-state)]
    (loop [input inputs]
      (cond
        (> (:quan (first input))
           (:quan (get user-comms (:title (first input))))) false
        (empty? input) true
        :else (recur (rest input))))))

(defn user-combine-for-comm [title]
  (doseq [input (get-in @user-state [:comm title :input])]
    (take-user-comm! (:title input) (:quan input)))
  (give-user-comm! title (get-in @user-state [:comm title :output-quan])))
