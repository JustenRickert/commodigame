(ns commgame.state.employees
  (:require [reagent.core :as r]
            [commgame.state.commodities :as comm]
            [commgame.state.data :as data]))

;; Could maybe do these with records and protocols. I think it's unnecessary, though.
;; (defrecord Manufacturer [key
;;                          title
;;                          price
;;                          time-interval
;;                          quan])
;; (defrecord Vendor [key
;;                    title
;;                    price
;;                    time-interval
;;                    quan])

;; (defrecord Processor [key
;;                       title
;;                       price
;;                       time-interval
;;                       quan])

(def start-quan 0)
(def time-interval-default 10)

(def manufacturers
  (into
   {}
   (for [manufacturer (->> data/c-comm
                           (map (fn [[title comm]]
                                  {:key           (:key comm)
                                   :type          :manufacturer
                                   :title         title
                                   :price         (:manufacturer-price comm)
                                   :quan          start-quan})))]
     [(:title manufacturer) (r/atom manufacturer)])))
(def manufacturers-time
  (into
   {}
   (for [manuf (->> data/c-comm
                    (map (fn [[title comm]]
                           {:title         title
                            :type          :manufacturer
                            :time-factor   1
                            :time-until    0
                            :time-interval time-interval-default})))]
     [(:title manuf) (r/atom manuf)])))

(def vendors
  (into
   {}
   (for [vendor (->> data/c-comm
                     (map (fn [[title comm]]
                            {:key   (:key comm)
                             :type  :vendor
                             :title title
                             :price (:vendor-price comm)
                             :quan  start-quan})))]
     [(:title vendor) (r/atom vendor)])))
(def vendors-time
  (into
   {}
   (for [vendor (->> data/c-comm
                     (map (fn [[title comm]]
                            {:title         title
                             :type          :vendor
                             :time-factor   1
                             :time-until    0
                             :time-interval time-interval-default})))]
     [(:title vendor) (r/atom vendor)])))

(def processors
  (into
   {}
   (for [processor (->> data/b-comm
                        (map (fn [[title comm]]
                               {:key   (:key comm)
                                :type  :processor
                                :title title
                                :price (:processor-price comm)
                                :quan  start-quan})))]
     [(:title processor) (r/atom processor)])))
(def processors-time
  (into
   {}
   (for [processor (->> data/b-comm
                        (map (fn [[title comm]]
                               {:title         title
                                :type          :processor
                                :time-factor   1
                                :time-until    0
                                :time-interval time-interval-default})))]
     [(:title processor) (r/atom processor)])))

(defn reset-time-until! [employ-type employ-title]
  (let [employ-time-atom (case employ-type
                           :processor    (get processors-time employ-title)
                           :manufacturer (get manufacturers-time employ-title)
                           :vendor       (get vendors-time employ-title))]
    (swap! employ-time-atom assoc :time-until
           (:time-interval @employ-time-atom))))

(defn dec-time!
  [{time         :time
    employ-type  :type
    employ-title :title}]
  {:pre [(some? time)
         (some? employ-type)
         (some? employ-title)]}
  (let [employ-time-atom (case employ-type
                           :processor    (get processors-time employ-title)
                           :manufacturer (get manufacturers-time employ-title)
                           :vendor       (get vendors-time employ-title))]
    (swap! employ-time-atom update :time-until
           #(- % (* time (:time-factor @employ-time-atom))))))

(defn hirable?
  ([employ-type employ-title]
   (hirable? employ-type employ-title 1))
  ([employ-type employ-title amount]
   (let [employ-atom (case employ-type
                       :processor    (get processors employ-title)
                       :manufacturer (get manufacturers employ-title)
                       :vendor       (get vendors employ-title))
         employ-cost (get @employ-atom :price)]
     (> (:amount @comm/money) (* amount employ-cost)))))

(defn hire!
  ([employ-type employ-title]
   (hire! employ-type employ-title 1))
  ([employ-type employ-title amount]
   (let [employ-atom (case employ-type
                         :processor    (get processors employ-title)
                         :manufacturer (get manufacturers employ-title)
                         :vendor       (get vendors employ-title))
         employ-cost (get @employ-atom :price)]
     (comm/dec-money! {:amount (* amount employ-cost)})
     (swap! employ-atom update :quan #(+ % amount)))))

;; (hire! :processor "sand")

(defn possibly-hire!
  ([employ-type employ-title]
   (possibly-hire! employ-type employ-title 1))
  ([employ-type employ-title amount]
   (when (hirable? employ-type employ-title amount)
     (hire! employ-type employ-title amount))))

