(ns commgame.component.vendor-table
  (:require [reagent.core :as r]
            [commgame.state.user :as user]
            [commgame.state.data :as data]))

(def ^:private component-state
  (r/atom {:sorted-vendors (sorted-map)
           :prev-sort-key  :title ;ALSO :price :quan
           :sort-key       :price
           :sort-forward   true}))

(defn sorting-table [])

