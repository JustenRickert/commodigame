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
(def time-interval-default 0)

(def manufacturers
  (r/atom (into
           {}
           (for [manufacturer (->> data/c-comm
                                   (map (fn [[title comm]]
                                          {:key           (:key comm)
                                           :type          :manufacturer
                                           :title         title
                                           :price         (:manufacturer-price comm)
                                           :time-interval time-interval-default
                                           :quan          start-quan})))]
             [(:title manufacturer) manufacturer]))))

(def vendors
  (r/atom (into
           {}
           (for [vendor (->> data/c-comm
                             (map (fn [[title comm]]
                                    {:key           (:key comm)
                                     :type          :vendor
                                     :title         title
                                     :price         (:vendor-price comm)
                                     :time-interval time-interval-default
                                     :quan          start-quan})))]
             [(:title vendor) vendor]))))

(def processors
  (r/atom (into
           {}
           (for [processor (->> data/b-comm
                                (map (fn [[title comm]]
                                       {:key           (:key comm)
                                        :type          :processor
                                        :title         title
                                        :price         (:processor-price comm)
                                        :time-interval time-interval-default
                                        :quan          start-quan})))]
             [(:title processor) processor]))))

(defn hirable?
  ([employ-type employ-title]
   (hirable? employ-type employ-title 1))
  ([employ-type employ-title amount]
   (let [employ-atom (case employ-type
                       :processor    processors
                       :manufacturer manufacturers
                       :vendor       vendors)
         employ-cost (get-in @employ-atom [employ-title :price])]
     (> (:amount @comm/money) (* amount employ-cost)))))

(hirable? :processor "glass")

(defn hire!
  ([employ-type employ-title]
   (hire! employ-type employ-title 1))
  ([employ-type employ-title amount]
   (let [employ-atom (case employ-type
                         :processor    processors
                         :manufacturer manufacturers
                         :vendor       vendors)
         employ-cost (get-in @employ-atom [employ-title :price])]
     (comm/dec-money! {:amount (* amount employ-cost)})
     (swap! employ-atom update-in [employ-title :quan] #(+ % amount)))))

;; (hire! :processor "sand")

(defn possibly-hire!
  ([employ-type employ-title]
   (possibly-hire! employ-type employ-title 1))
  ([employ-type employ-title amount]
   (when (hirable? employ-type employ-title amount)
     (hire! employ-type employ-title amount))))

