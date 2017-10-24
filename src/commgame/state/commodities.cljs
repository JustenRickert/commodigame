(ns commgame.state.commodities
  (:require [reagent.core :as r]
            ;; [commgame.state.employees :as employ]
            [commgame.state.data :as data]))

(defonce money
  (r/atom {:amount 100
           :factor 0.05}))
(defonce inventory
  (r/atom {:b-comm (into
                    {}
                    (for [[title item] data/b-comm]
                      {title (assoc item :quan 0 :title title)}))
           :c-comm (into
                    {}
                    (for [[title item] data/c-comm]
                      {title (assoc item :quan 0 :title title)}))}))

(defn dec-money!
  ([{time :time amt :amount}]
   {:pre [(some? amt)]}
   (swap! money update :amount #(- % (* (or time 1)
                                        amt)))))

(defn inc-money!
  ([]
   (swap! money update :amount #(+ % (:money-factor @money))))
  ([{time :time
     amt :amount}]
   (swap! money update :amount #(+ % (* (or time 1)
                                        (or amt (:factor @money)))))
   #_(.log js/console time)))

(defn max-buyable
  [comm-type comm-title]
  (let [comm-price (get-in @inventory [comm-type comm-title :price])]
    (quot (:amount @money) comm-price)))

(defn buyable?
  ([comm-type comm-title]
   (buyable? comm-type comm-title 1))
  ([comm-type comm-title amt]
   (let [comm-price (get-in @inventory [comm-type comm-title :price])]
     (> (:amount @money) (* amt comm-price)))))

(defn buy!
  ([comm-type comm-title]
   (buy! comm-type comm-title 1))
  ([comm-type comm-title amt]
   (let [comm-price  (get-in @inventory [comm-type comm-title :price])]
     (swap! inventory update-in [comm-type comm-title :quan] #(+ % amt))
     (swap! money update :amount #(- % (* comm-price amt))))))

(defn possibly-buy!
  ([comm-type comm-title]
   (possibly-buy! comm-type comm-title 1))
  ([comm-type comm-title amt]
   (when (buyable? comm-type comm-title amt)
     (buy! comm-type comm-title amt))))

(defn combinable? [title]
  (let [input (get-in @inventory [:c-comm title :input])
        b-comms (:b-comm @inventory)]
    (every? #(>= (:quan (get b-comms (:title %)))
                 (:quan %))
            input)))

(defn combine! [title]
  (doseq [input (get-in @inventory [:c-comm title :input])]
    (swap! inventory update-in [:b-comm (:title input) :quan] #(- % (:quan input))))
  (swap! inventory update-in [:c-comm title :quan]
         #(+ % (get-in @inventory [:c-comm title :output-quan]))))

(defn possibly-combine! [title]
  (when (combinable? title)
    (combine! title)))

(defn sellable? [title])

(defn sell! [title])
