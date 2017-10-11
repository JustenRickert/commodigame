(ns commgame.core
  (:require [reagent.core :as reagent :refer [atom]]

            [commgame.commodities :as comm]
            [commgame.game :as game]
            [commgame.render :as render]))

(enable-console-print!)

(reagent/render-component [render/page]
                          (. js/document (getElementById "app")))
(game/timer-loop!)

#_(defn on-js-reload
  []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
)
