(ns commgame.commodities
  (:require [commgame.core :as game]
            ;; [cljs-time.core :as time]
            ;; [cljs-time.format :as time-format]
            ;; [goog.string :as string]
            ;; [reagent.core :as reagent :refer [atom]]
))

;; comm is commodity, quan is quantity, comb is combination. Very difficult
;; words to spell a lot!

(defonce comm-data
  (atom  [{:title "sand"  :price 0.25}
          {:title "hops"  :price 0.50}
          {:title "water" :price 0.10}
          {:title "fruit" :price 1.00}

          {:title "glass"            :price 2.15}
          {:title "ice"              :price 0.75}
          {:title "ice water"        :price 0.90}
          {:title "fruit drink"      :price 2.15}
          {:title "cold fruit drink" :price 3.10}
          {:title "beer"             :price 4.00}
          {:title "lager"            :price 3.95}
          {:title "radler"           :price 5.35}]))

(defonce comm-combs
  (atom  [{:input [{:title "sand"  :quan 5}]
           :output {:title "glass" :quan 1}}

          {:input [{:title "water" :quan 5}]
           :output {:title "ice"   :quan 6}}

          {:input [{:title "water"     :quan 5}
                   {:title "ice"       :quan 2}]
           :output {:title "ice water" :quan 1}}

          {:input [{:title "hops"  :quan 10}
                   {:title "water" :quan 15}
                   {:title "glass" :quan 15}]
           :output {:title "beer"  :quan 10}}

          {:input [{:title "hops"  :quan 10}
                   {:title "water" :quan 15}
                   {:title "ice"   :quan 15}
                   {:title "glass" :quan 15}]
           :output {:title "lager" :quan 10}}

          {:input [{:title "beer"             :quan 2}
                   {:title "cold fruit drink" :quan 1}]
           :output {:title "radler"           :quan 1}}

          {:input [{:title "water"       :quan 2}
                   {:title "fruit"       :quan 1}]
           :output {:title "fruit drink" :quan 1}}

          {:input [{:title "fruit drink"      :quan 1}
                   {:title "ice"              :quan 1}]
           :output {:title "cold fruit drink" :quan 1}}]))

(defn comm-titles-with-combs []
  ;; sets make using the contains? predicate actually useful
  (set (map #(:title (:output %)) @comm-combs)))

(defn get-comb-inputs-by-title [title]
  (loop [comb @comm-combs]
    (let [input (:input (first comb))
          output (:output (first comb))]
      (cond (= title (:title output)) input
            (empty? comb) nil
            :else (recur (rest comb))))))

(defn get-comb-output-by-title [title]
  (loop [comb @comm-combs]
    (let [input (:input (first comb))
          output (:output (first comb))]
      (cond (= title (:title output)) output
            (empty? comb) nil
            :else (recur (rest comb))))))
