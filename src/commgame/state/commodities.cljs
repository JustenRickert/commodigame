(ns commgame.state.commodities
  (:require [reagent.core :as r]
            ;; [commgame.state.employees :as employ]
            [commgame.state.data :as data]))

(def money
  (r/atom {:amount 1000
           :factor 0.05}))
(def inventory
  {:b-comm (into
            {}
            (for [[title item] data/b-comm]
              {title (r/atom (assoc item :quan 0 :title title))}))
   :c-comm (into
            {}
            (for [[title item] data/c-comm]
              {title (r/atom (assoc item :quan 0 :title title))}))})

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
                                        (or amt (:factor @money)))))))

(defn max-buyable
  [comm-type comm-title]
  (let [comm-a (get-in inventory [comm-type comm-title])]
    (quot (:amount @money) (:price @comm-a))))

(defn buyable?
  ([comm-type comm-title]
   (buyable? comm-type comm-title 1))
  ([comm-type comm-title amt]
   (let [comm-a (get-in inventory [comm-type comm-title])]
     (> (:amount @money) (* amt (:price @comm-a))))))

(defn buy!
  ([comm-type comm-title]
   (buy! comm-type comm-title 1))
  ([comm-type comm-title amt]
   (let [comm-a  (get-in inventory [comm-type comm-title])]
     (swap! money update :amount #(- % (* (:price @comm-a) amt)))
     (swap! comm-a update :quan #(+ % amt)))))

(defn possibly-buy!
  ([comm-type comm-title]
   (possibly-buy! comm-type comm-title 1))
  ([comm-type comm-title amt]
   (when (buyable? comm-type comm-title amt)
     (buy! comm-type comm-title amt))))

(defn combinable? [title]
  (let [comm-a (get-in inventory [:c-comm title])
        ;; There's gotta be a better way to do this. This copies the whole
        ;; structure just to make the deref...
        b-comm (into {} (map #(vector (key %) (-> % val deref)) (:b-comm inventory)))]
    (every? #(>= (:quan (get b-comm (:title %)))
                 (:quan %))
            (:input @comm-a))))

(defn combine! [title]
  (let [c-comm-a (get-in inventory [:c-comm title])]
    (doseq [input (:input @c-comm-a)]
      (let [b-comm-a (get-in inventory [:b-comm (:title input)])]
        (swap! b-comm-a update :quan #(- % (:quan input)))))
    (swap! c-comm-a update :quan #(+ % (:output-quan @c-comm-a)))))

(defn possibly-combine! [title]
  (when (combinable? title)
    (combine! title)))

(defn max-sellable
  [comm-type comm-title]
  (let [comm-a (get-in inventory [comm-type comm-title])]
    (:quan @comm-a)))

(defn sellable?
  ([comm-type comm-title amt]
   (let [comm-a (get-in inventory [comm-type comm-title])]
     (>= (:quan @comm-a) amt))))

(defn sell!
  ([comm-type comm-title]
   (sell! comm-type comm-title 1))
  ([comm-type comm-title amt]
   (let [comm-a (get-in inventory [comm-type comm-title])]
     (swap! money update :amount #(+ % (* (:price @comm-a) amt)))
     (swap! comm-a update :quan #(- % amt)))))

(defn possibly-sell!
  ([comm-type comm-title]
   (possibly-sell! comm-type comm-title 1))
  ([comm-type comm-title amt]
   (when (sellable? comm-type comm-title amt)
     (sell! comm-type comm-title amt))))
