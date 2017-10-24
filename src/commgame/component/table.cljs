(ns commgame.component.table
  (:require [reagent.core :as r]
            [commgame.state.commodities :as comm]
            [commgame.state.employees :as employ]))

(defmulti buy-button :type)
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

;; TODO this table-row function should be ordered. Should be easy to do.

(defmulti table-row :type)
(defmethod table-row :b-comm [item-map]
  [:tr
   [:td (:title item-map)]
   [:td (:quan item-map)]
   [:td (:price item-map)]
   [:td [buy-button {:type  :b-comm
                     :title (:title item-map)}]]])

(defmethod table-row :c-comm [item-map]
  (when (or (comm/combinable? (:title item-map))
            (> (:quan item-map) 0))
    [:tr
     [:td (:title item-map)]
     [:td (:quan item-map)]
     [:td (:price item-map)]
     [:td [buy-button {:type  :c-comm
                       :title (:title item-map)}]]]))

(defmethod table-row :processor [employ-map]
  [:tr
   [:td (:title employ-map)]
   [:td (:quan employ-map)]
   [:td (:price employ-map)]
   [:td [buy-button {:type  :processor
                     :title (:title employ-map)}]]])

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

(defn- toggle-truth [truth] (not truth))

(defn update-sort-state [table-state-atom clicked-table-key]
  (swap! table-state-atom update :prev-sort-key
         #(:sort-key @table-state-atom))
  (swap! table-state-atom update :sort-key
         (constantly clicked-table-key))
  (if (= (:sort-key @table-state-atom) (:prev-sort-key @table-state-atom))
    (swap! table-state-atom update :sort-forward toggle-truth)
    (swap! table-state-atom update :sort-forward (constantly true))))

(defn- table
  [{what-type            :type
    state-map            :state
    table-sort-state-atom :sort-atom
    [& table-row-params] :table-row}]
  {:pre [(some? what-type)
         (some? state-map)
         (some? table-row-params)
         (some? table-sort-state-atom)]}
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
                         :price (sort-by #(-> % val :price) > state-map)
                         :quan  (sort-by #(-> % val :quan) > state-map)
                         (throw (js/Error. "You need to add a case for sorting key"
                                           (:sort-key @table-sort-state-atom))))]
                   (if (:sort-forward @table-sort-state-atom)
                     sorted-state-map
                     (rseq sorted-state-map)))]
             ^{:key (str "tr-" title)}
             [table-row (merge {:type what-type}
                               (into
                                {}
                                (map #(vector % (% comm)) table-row-params)))]))]])

(defn b-comm []
  (table {:type      :b-comm
          :state     (:b-comm @comm/inventory)
          :sort-atom table-b-comm-sort-state
          :table-row [:title :quan :price]}))

(defn c-comm []
  (table {:type      :c-comm
          :state     (:c-comm @comm/inventory)
          :sort-atom table-c-comm-sort-state
          :table-row [:title :price :quan]}))

;; (defn c-comm []
;;   (table {:type  :c-comm
;;           :state (:c-comm @comm/inventory)}))

;; (defn processor []
;;   (table {:type  :processor
;;           :state @employ/processors}))


;; (defmulti table :type)

;; (defmethod table :b-comm [comm-map]
;;   ;; (.log js/console "rerender???")
;;   [:table
;;    [:thead
;;     [:tr
;;      [:th {:on-click #(update-sort-state table-b-comm-sort-state :title)} "title"]
;;      [:th {:on-click #(update-sort-state table-b-comm-sort-state :quan)} "quan"]
;;      [:th {:on-click #(update-sort-state table-b-comm-sort-state :price)} "price"]]]
;;    [:tbody
;;     (doall
;;      (remove nil?
;;              (for [[title comm]
;;                    (let [sorted-comms
;;                          (case (:sort-key @table-b-comm-sort-state)
;;                            :title (sort-by key (:state comm-map))
;;                            :price (sort-by #(-> % val :price) > (:state comm-map))
;;                            :quan  (sort-by #(-> % val :quan) < (:state comm-map)))]
;;                      (if (:sort-forward @table-b-comm-sort-state)
;;                        sorted-comms
;;                        (rseq sorted-comms)))]
;;                ;; when (> (:quan comm) 0)
;;                (do
;;                  ;; (js/console.log title)
;;                  ^{:key (str "tr-" title)}
;;                  [table-row {:title title
;;                              :type  :b-comm
;;                              :quan  (:quan comm)
;;                              :price (:price comm)}]))))]])

;; (defmethod table :c-comm [comm-map]
;;   [:table
;;    [:thead
;;     [:tr
;;      [:th {:on-click #(update-sort-state table-c-comm-sort-state :title)} "title"]
;;      [:th {:on-click #(update-sort-state table-c-comm-sort-state :quan)} "quan"]
;;      [:th {:on-click #(update-sort-state table-c-comm-sort-state :price)} "price"]]]
;;    [:tbody
;;     (doall
;;      (remove nil?
;;              (for [[title comm]
;;                    (let [sorted-comms
;;                          (case (:sort-key @table-c-comm-sort-state)
;;                            :title (sort-by key (:state comm-map))
;;                            :price (sort-by #(-> % val :price) > (:state comm-map))
;;                            :quan  (sort-by #(-> % val :quan) < (:state comm-map)))]
;;                      (if (:sort-forward @table-c-comm-sort-state)
;;                        sorted-comms
;;                        (rseq sorted-comms)))]
;;                (when (or (comm/combinable? title) (> (:quan comm) 0))
;;                  ^{:key (str "tr-" title)}
;;                  [table-row {:title title
;;                              :type  :c-comm
;;                              :quan  (:quan comm)
;;                              :price (:price comm)}]))))]])

;; (defmethod table :processor [employ-map]
;;   [:table
;;    [:thead
;;     [:tr
;;      [:th {:on-click #(update-sort-state table-processor-sort-state :title)} "title"]
;;      [:th {:on-click #(update-sort-state table-processor-sort-state :quan)} "quan"]
;;      [:th {:on-click #(update-sort-state table-processor-sort-state :price)} "price"]]]
;;    [:tbody
;;     (doall
;;      (remove nil?
;;              (for [[title comm]
;;                    (let [sorted-comms
;;                          (case (:sort-key @table-processor-sort-state)
;;                            :title (sort-by key (:state employ-map))
;;                            :price (sort-by #(-> % val :price) > (:state employ-map))
;;                            :quan  (sort-by #(-> % val :quan) < (:state employ-map)))]
;;                      (if (:sort-forward @table-procesor-sort-state)
;;                        sorted-comms
;;                        (rseq sorted-comms)))]
;;                ^{:key (str "tr-" title)}
;;                [table-row {:title title
;;                            :type  :processor
;;                            :quan  (:quan comm)
;;                            :price (:price comm)}])))]])
