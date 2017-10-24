(ns commgame.component.table
  (:require [reagent.core :as r]
            [commgame.state.commodities :as comm]
            [commgame.state.employees :as employ]))

(def ^:private table-b-comm-sort-state
  (r/atom {:prev-sort-key :title
           :sort-key      :price
           :sort-forward  true}))
(def ^:private table-c-comm-sort-state
  (r/atom {:prev-sort-key :title
           :sort-key      :price
           :sort-forward  true}))
(def ^:private table-processor-sort-state
  (r/atom {:prev-sort-key :title
           :sort-key      :price
           :sort-forward  true}))
(def ^:private table-vendor-sort-state
  (r/atom {:prev-sort-key :title
           :sort-key      :price
           :sort-forward  true}))
(def ^:private table-manufacturer-sort-state
  (r/atom {:prev-sort-key :title
           :sort-key      :price
           :sort-forward  true}))

(defn- toggle-truth [truth] (not truth))

(defn update-sort-state [table-state-atom clicked-table-key]
  (swap! table-state-atom update :prev-sort-key
         #(:sort-key @table-state-atom))
  (swap! table-state-atom update :sort-key
         (constantly clicked-table-key))
  (if (= (:sort-key @table-state-atom) (:prev-sort-key @table-state-atom))
    (swap! table-state-atom update :sort-forward toggle-truth)
    (swap! table-state-atom update :sort-forward (constantly true))))

(defmulti ^:private buy-button :type)

(defmethod buy-button :b-comm [item]
  (let [title (:title item)]
    [:button {:on-click #(comm/possibly-buy! (:type item) title)}
     "BuY!"]))

(defmethod buy-button :c-comm [item]
  (let [title (:title item)]
    [:button {:on-click #(comm/possibly-combine! title)}
     "CoMbInE!"]))

(defmethod buy-button :processor [employ]
  (let [title (:title employ)]
    [:button {:on-click #(employ/possibly-hire! :processor title)}
     "HiRe!"]))

(defmethod buy-button :vendor [employ]
  (let [title (:title employ)]
    [:button {:on-click #(employ/possibly-hire! :vendor title)}
     "HiRe!"]))

(defmethod buy-button :manufacturer [employ]
  (let [title (:title employ)]
    [:button {:on-click #(employ/possibly-hire! :manufacturer title)}
     "HiRe!"]))

;; TODO this table-row function should be ordered. Should be easy to do.

(defn- table-row [item-map]
  (let [comm-a        (case (:type item-map)
                        :b-comm       (get-in comm/inventory [:b-comm (:title item-map)])
                        :processor    (get-in comm/inventory [:b-comm (:title item-map)])
                        :c-comm       (get-in comm/inventory [:c-comm (:title item-map)])
                        :manufacturer (get-in comm/inventory [:c-comm (:title item-map)])
                        :vendor       (get-in comm/inventory [:c-comm (:title item-map)]))
        able?         (case (:type item-map)
                        :c-comm       (or (comm/combinable? (:title item-map))
                                          (> (:quan item-map) 0))
                        :vendor       (> (:quan @comm-a) 0)
                        :manufacturer (> (:quan @comm-a) 0)
                        (constantly true))
        map-sans-type (filter #(not= (key %) :type) item-map)]
    (when able?
      [:tr
       (for [[key val] map-sans-type]
         ^{:key (str "tr-" key "-" (:title item-map))}
         [:td val])
       [:td [buy-button {:type  (:type item-map)
                         :title (:title item-map)}]]])))

(defn- table
  [{what-type             :type
    state-map             :state
    table-sort-state-atom :sort-atom
    [& table-row-params]  :table-row}]
  {:pre [(some? what-type)
         (some? state-map)
         (some? table-row-params)
         (some? table-sort-state-atom)]}
  ;; (js/console.log (map #(-> % val deref :price) state-map))
  [:table
   [:thead
    [:tr
     (for [[key key-name] (map #(into [] [(identity %) (name %)]) table-row-params)]
       ^{:key (str "thr-" key)}
       [:th {:on-click #(update-sort-state table-sort-state-atom key)} key-name])]]
   [:tbody
    (doall (for [[title comm]
                 (let [sorted-state-map
                       (case (:sort-key @table-sort-state-atom)
                         :title (sort-by key state-map)
                         :price (sort-by #(-> % val deref :price) > state-map)
                         :quan  (sort-by #(-> % val deref :quan) > state-map)
                         (throw (js/Error. "You need to add a case for sorting key"
                                           (:sort-key @table-sort-state-atom))))]
                   ;; (js/console.log sorted-state-map)
                   (if (:sort-forward @table-sort-state-atom)
                     sorted-state-map
                     (rseq sorted-state-map)))]
             (do
               ;; (js/console.log (:title @comm))
               ^{:key (str "tr-" title)}
               [table-row (merge {:type what-type}
                                 (into
                                  {}
                                  (map #(vector % (% @comm)) table-row-params)))])))]])

(defn b-comm []
  (table {:type      :b-comm
          :state     (:b-comm comm/inventory)
          :sort-atom table-b-comm-sort-state
          :table-row [:title :quan :price]}))

(defn c-comm []
  (table {:type      :c-comm
          :state     (:c-comm comm/inventory)
          :sort-atom table-c-comm-sort-state
          :table-row [:title :price :quan]}))

(defn processor []
  (table {:type      :processor
          :table-row [:title :price :quan]
          :sort-atom table-processor-sort-state
          :state     employ/processors}))

(defn manufacturer []
  (table {:type      :manufacturer
          :table-row [:title :price :quan]
          :sort-atom table-manufacturer-sort-state
          :state     employ/manufacturers}))

(defn vendor []
  (table {:type      :vendor
          :table-row [:title :price :quan]
          :sort-atom table-vendor-sort-state
          :state     employ/vendors}))
