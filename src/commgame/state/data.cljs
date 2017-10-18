(ns commgame.state.data)

;; comm is commodity, quan is quantity, comb is combination. Very difficult
;; words to spell a lot!

(def ^:private b-comm-before-id
  {"sand"
   {:price       0.25
    :merch-price 12.00}

   "sugar"
   {:price       0.50
    :merch-price 24.00}

   "hops"
   {:price       0.50
    :merch-price 24.00}

   "water"
   {:price       0.10
    :merch-price 5.00}

   "fruit"
   {:price       1.00
    :merch-price 48.00}})

(def ^:private c-comm-before-id
  {"glass" ; single input combinational commodities
   {:price        2.15
    :vendor-price 100.00
    :input        [{:title "sand" :quan 5}]
    :output-quan  3}

   "ice"
   {:price        0.75
    :vendor-price 30.00
    :input        [{:title "water" :quan 5}]
    :output-quan  6}

   "crushed fruit"
   {:price        1.10
    :vendor-price 60.00
    :input        [{:title "fruit" :quan 3}]
    :output-quan  4}

   "ice water" ; multiple input combinational commodities
   {:price        0.90
    :vendor-price 50.00
    :input        [{:title "water" :quan 4}
                   {:title "ice" :quan 1}]
    :output-quan  5}

   "fruit drink"
   {:price        2.15
    :vendor-price 110.00
    :input        [{:title "water" :quan 2}
                   {:title "sugar" :quan 2}
                   {:title "fruit" :quan 1}]
    :output-quan  5}

   "cold fruit drink"
   {:price        3.10
    :vendor-price 135.00
    :input        [{:title "ice" :quan 1}
                   {:title "fruit drink" :quan 2}]
    :output-quan  2}

   "beer"
   {:price        4.00
    :vendor-price 180.00
    :input        [{:title "hops" :quan 1}
                   {:title "water" :quan 2}
                   {:title "glass" :quan 3}]
    :output-quan  5}

   "lager"
   {:price        3.95
    :vendor-price 175.00
    :input        [{:title "hops" :quan 1}
                   {:title "water" :quan 2}
                   {:title "ice" :quan 1}
                   {:title "glass" :quan 4}]
    :output-quan  6}

   "radler"
   {:price        5.35
    :vendor-price 125.00
    :input        [{:title "beer" :quan 1}
                   {:title "cold fruit drink" :quan 1}]
    :output-quan  2}})

;; Want to add a random id to each one because it helps react do things quickly.
;; The console also complains otherwise.
(def b-comm
  ^{:doc "Basic commodities data. Commodities that cannot be created through
          combination"}
  (into {} (for [[k v] b-comm-before-id] {k (assoc v :key (random-uuid))})))

(def c-comm
  ^{:doc "Combinational commodities data. Commodities that can be created
          through combination of any basic commodities or other combinational
          commodities"}
  (into {} (for [[k v] c-comm-before-id] {k (assoc v :key (random-uuid))})))
