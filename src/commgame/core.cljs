(ns commgame.core
  (:require [commgame.commodities :as comm]
            [commgame.render :as render]

            [cljs-time.core :as time]
            [cljs-time.format :as time-format]
            [goog.string :as string]
            [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(def time-formatter (time-format/formatter "yyyy/MM/dd hh:mm:ss UTC"))

;; Note that atom is a reagent atom here
(defonce app-state
  (atom {:begin-time (time/now)
         :time-now (time/now)
         :time-last-tick (time/now)

         :user {:money 0
                :money-delta 0.1
                :commodities (map #(assoc %
                                          :quan 0
                                          ;; reagent is more efficient when each
                                          ;; item in a collection has a unique
                                          ;; key. Here, two 128-bit uuids have a
                                          ;; 1 in 2^256 chance of being the
                                          ;; same.
                                          :key (random-uuid)) @comm/comm-data)}}))

(defn seconds-since-begin-time []
  (time/in-seconds (time/interval (:begin-time @app-state) (time/now))))

(defn seconds-since-last-tick []
  (time/in-seconds (time/interval (:time-last-tick @app-state) (time/now))))

(defn user-commodities []
  (get-in @app-state [:user :commodities]))

(defn give-user-money! [amt]
  (swap! app-state update-in [:user :money]
         #(+ (get-in @app-state [:user :money]) amt)))

(defn take-user-money! [amt]
  (swap! app-state update-in [:user :money]
         #(- (get-in @app-state [:user :money]) amt)))

(defn get-comm-by-title [title]
  (loop [comm (get-in @app-state [:user :commodities])]
    (cond (= title (:title (first comm))) (first comm)
          (empty? comm) nil
          :else (recur (rest comm)))))

;; In order to access the comm by index, the app-state user commodities must be
;; made into a vector. It sits in app-state as a map because it is seqable,
;; meaning it's useable in loops like in this function. I actually may be wrong
;; about this. But I bypassed a runtime exception by not using a vec
(defn get-comm-by-index [index]
  (loop [comm (get-in @app-state [:user :commodities])
         i 0]
    (cond (= i index) (first comm)
          (empty? comm) nil
          :else (recur (rest comm) (inc i)))))

;; I have a feeling the first version is better, but it probably doesn't matter
;; either way.
;; (defn get-comm-by-index [index]
;;   (get (vec (get-in @app-state [:user :commodities])) index))

(defn index-of-user-comm [title]
  (loop [comm (get-in @app-state [:user :commodities])
         index 0]
    (cond (= title (:title (first comm))) index
          (empty? comm) nil
          :else (recur (rest comm) (inc index)))))

(defn give-user-comm! [title amt]
  (let [user-comm (get-comm-by-title title)]
    (swap! app-state update-in [:user :commodities]
           (fn [] (map #(if (= (:title %) title)
                          (assoc % :quan (+ (:quan user-comm) amt))
                          (identity %))
                       (vec (get-in @app-state [:user :commodities])))))))

(defn take-user-comm! [title amt]
  (let [user-comm (get-comm-by-title title)]
    (swap! app-state update-in [:user :commodities]
           (fn [] (map #(if (= (:title %) title)
                         (assoc % :quan (- (:quan user-comm) amt))
                         (identity %))
                      (vec (get-in @app-state [:user :commodities])))))))

(defn user-commodity-of-id [id]
  (get-in @app-state [:user :commodities id]))

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
  (let [comm-buying (get-comm-by-title title)]
    (if (> (get-in @app-state [:user :money]) (:price comm-buying))
      (do
        (take-user-money! (:price comm-buying))
        (give-user-comm! (:title comm-buying) 1))
      (js/console.log "Not enough money to buy" title "!"))))


(defn user-can-combine-for-comm [title]
  (let [comb-inputs (comm/get-comb-inputs-by-title title)
        user-comms (user-commodities)]
    (loop [input comb-inputs]
      ;; (js/console.log comb-inputs)
      (cond
        ;; user quantity less than required input quantity
        (< (:quan (get-comm-by-title (:title (first input))))
           (:quan (first input))) false
        (empty? input) true
        :else (recur (rest input))))))

(defn user-combine-for-comm [title]
  ;; (if (user-can-combine-for-comm title) nil (js/console.log "Cannot combine for" title "!"))
  (let [comb-inputs (comm/get-comb-inputs-by-title title)
        comb-output (comm/get-comb-output-by-title title)]
    (js/console.log (:quan comb-inputs) (:quan comb-output))
    (doseq [input comb-inputs]
      (take-user-comm! (:title input) (:quan input)))
    (give-user-comm! (:title comb-output) (:quan comb-output))))

(reagent/render-component [render/page]
                          (. js/document (getElementById "app")))
(timer-loop!)

#_(defn on-js-reload
  []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
)
