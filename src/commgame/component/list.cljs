(ns commgame.component.list)

(defmulti list-item (fn [item] (:type item)))
(defmethod list-item :all-comm [] ())
