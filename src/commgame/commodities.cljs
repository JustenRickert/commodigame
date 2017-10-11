(ns commgame.commodities)

;; comm is commodity, quan is quantity, comb is combination. Very difficult
;; words to spell a lot!

(defonce b-comm-data-before-id
  [{:title "sand"  :price 0.25}
   {:title "sugar" :price 0.50}
   {:title "hops"  :price 0.50}
   {:title "water" :price 0.10}
   {:title "fruit" :price 1.00}])

(defonce c-comm-data-before-id
  [; single input combinational commodities
   {:title "glass"
    :price 2.15
    :input [{:title "sand"  :quan 5}]
    :output-quan 3}

   {:title "ice"
    :price 0.75
    :input [{:title "water" :quan 5}]
    :output-quan 6}

   {:title "crushed fruit"
    :price 1.10
    :input [{:title "fruit" :quan 3}]
    :output-quan 4}

                                        ; multiple input combinational commodities
   {:title "ice water"
    :price 0.90
    :input [{:title "water" :quan 4}
            {:title "ice"   :quan 1}]
    :output-quan 5}

   {:title "fruit drink"
    :price 2.15
    :input [{:title "water" :quan 2}
            {:title "sugar" :quan 2}
            {:title "fruit" :quan 1}]
    :output-quan 5}

   {:title "cold fruit drink"
    :price 3.10
    :input [{:title "ice"         :quan 1}
            {:title "fruit drink" :quan 2}]
    :output-quan 2}

   {:title "beer"
    :price 4.00
    :input [{:title "hops"  :quan 1}
            {:title "water" :quan 2}
            {:title "glass" :quan 3}]
    :ouput-quan 5}

   {:title "lager"
    :price 3.95
    :input [{:title "hops"  :quan 1}
            {:title "water" :quan 2}
            {:title "ice"   :quan 1}
            {:title "glass" :quan 4}]
    :output-quan 6}

   {:title "radler"
    :price 5.35
    :input [{:title "beer"             :quan 1}
            {:title "cold fruit drink" :quan 1}]
    :output-quan 2}])

;; Want to add a random id to each one because it helps react do things quickly.
;; The console also complains otherwise.

(defonce b-comm-data
  ^{:doc "Basic commodities data. Commodities that cannot be created through
          combination"}
  (mapv #(assoc % :key (random-uuid)) b-comm-data-before-id))

(defonce c-comm-data
  ^{:doc "Combinational commodities data. Commodities that can be created
          through combination of any basic commodities or other combinational
          commodities"}
  (mapv #(assoc % :key (random-uuid)) c-comm-data-before-id))

(defn comb-inputs-of-title [title]
  (loop [c-comm c-comm-data]
    (let [inputs (:input (first c-comm))]
      (cond (= title (:title (first c-comm))) inputs
            (empty? c-comm) nil
            :else (recur (rest c-comm))))))
