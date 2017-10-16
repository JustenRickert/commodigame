(ns commgame.vendor
  (:require [reagent.core :as reagent :refer [atom]]

            [commgame.commodities :as comm]
            [cljs-time.core :as time]
            [commgame.user :as user]))

(defn sell-comm! [amt title]
  (let [comm (get-in @user/state [:comm title])]
    (if (>= (:quan comm) amt)
      (do
        (user/take-user-comm! title amt)
        (user/give-user-money! (* amt (:price comm))))
      (.log js/console "Can't sell" amt title))))
