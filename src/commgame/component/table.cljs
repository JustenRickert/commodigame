(ns commgame.component.table
  (:require [reagent.core :as r]
            [commgame.state.user :as user]
            [commgame.state.data :as data]))

(defn- table-row
  [item-map]
  [:tr
   (for [[key td] item-map]
     ^{:key (str "td-" key)}
     [:td td])])

(def ^:private table-comm-sort-state
  (r/atom {:prev-sort-key :title ;ALSO :price :quan
           :sort-key      :price
           :sort-forward  true}))

(defn- toggle-truth [truth]
  (if truth
    false
    true))

(defn update-sort-state [clicked-table-key]
  (swap! table-comm-sort-state update :prev-sort-key
         #(:sort-key @table-comm-sort-state))
  (swap! table-comm-sort-state update :sort-key
         (constantly clicked-table-key))
  (if (= (:sort-key table-comm-sort-state)
         (:prev-sort-key table-comm-sort-state))
    (swap! table-comm-sort-state update :sort-forward toggle-truth)
    (swap! table-comm-sort-state update :sort-forward (constantly true))))

(defmulti table :data/type)
(defmethod table :comm [comm-map]
  [:table
   [:thead
    [:tr
     [:th {:on-click #(update-sort-state :title)} "title"]
     [:th {:on-click #(update-sort-state :price)} "price"]
     [:th {:on-click #(update-sort-state :quan)} "quan"]]]
   [:tbody
    (doall
     (remove nil?
             (for [[title comm]
                   (let [sorted-comms
                         (case (:sort-key @table-comm-sort-state)
                           :title (sort-by key (:map comm-map))
                           :price (sort-by val (juxt :price) (:map comm-map))
                           :quan (sort-by val (juxt :quan) (:map comm-map)))]
                     (if (:sort-forward @table-comm-sort-state)
                       sorted-comms
                       (rseq sorted-comms)))]
               (when (> (:quan comm) 0)
                 ^{:key (str "tr-" title)}
                 [table-row {:title title
                             :price (:price comm)
                             :quan  (:quan comm)}]))))]])
(defn comm-table [] (table {:data/type :comm
                            :map       (:comm @user/state)}))
