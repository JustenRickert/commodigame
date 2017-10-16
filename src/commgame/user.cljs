(ns commgame.user
  (:require [cljs-time.core :as time]
            [cljs-time.format :as time-format]
            [goog.string :as string]
            [reagent.core :as r]

            [commgame.commodities :as comm]))

(defn add-zero-quantity [item] (assoc item :quan 0))

(defonce perf-time
  (atom (js/performance.now)))

;; performance.now returns ms, and there are 60 fps.
(defn time-between-frame []             ;in seconds
  (-> (- (js/performance.now) @perf-time)
      (/ 1000)))

(defn reset-perf-time! []
  (reset! perf-time (js/performance.now)))

(defonce state
  (r/atom {:money        0
           :money-factor 0.05
           :comm         (into {}
                               (for [[title item] (merge comm/b-comm-data
                                                         comm/c-comm-data)]
                                 {title (assoc item :quan 0)}))}))

(defn c-comm []
  (into {}
        (for [[title _] comm/c-comm-data]
          {title (get-in @state [:comm title])})))

(def time-formatter (time-format/formatter "yyyy/MM/dd hh:mm:ss UTC"))

;; `!` denotes state mutation.
(defn give-user-money! [amt]
  (swap! state update :money #(+ (:money @state) amt)))

(defn take-user-money! [amt]
  (swap! state update :money #(- (:money @state) amt)))

;; TODO Most of these functions with `title` parameter could probably be made
;; into multimethods taking either a `comm` or a `title`. Some of the names
;; would need to be changed also. I think it's not idiomatic Clojure to use such
;; lengthy titles (which would be elided with multimethods).

(defn give-user-comm! [title amt]
  (swap! state update-in [:comm title :quan] #(+ % amt)))

(defn take-user-comm! [title amt]
  (swap! state update-in [:comm title :quan] #(- % amt)))

;; TODO This should use time differences in calculations to account for low
;; frame rate and tab halt/close situations. Shouldn't be difficult at all.

(defn timer-loop! []
  (do
    ;; increment user money
    (swap! state update :money #(+ % (* (time-between-frame)
                                        (:money-factor @state)))))
    ;; increment things to display the time
    ;; (swap! time-state update :time-last-tick time/now)
    ;; (swap! time-state update :time-now time/now))
  (reset-perf-time!)
  (js/requestAnimationFrame timer-loop!))

(defn buy-one-comm [title]
  (let [comm-buying (get (:comm @state) title)]
    (if (> (:money @state) (:price comm-buying))
      (do
        (js/console.log "Buying" title)
        (take-user-money! (:price comm-buying))
        (give-user-comm! title 1))
      (js/console.log "Not enough money to buy" title "!"))))

(defn c-comm-combinable? [title]
  (let [input (get-in (:comm @state) [title :input])
        user-comms (:comm @state)]
    (every? #(>= (:quan (get user-comms (:title %)))
                 (:quan %))
            input)))

(defn user-combine-for-comm [title]
  (doseq [input (get-in @state [:comm title :input])]
    (take-user-comm! (:title input) (:quan input)))
  (give-user-comm! title (get-in @state [:comm title :output-quan])))
