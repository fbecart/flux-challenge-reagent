(ns flux-challenge-reagent.sith-lords
    (:require [reagent.core :as r]
              [flux-challenge-reagent.util :as util]
              [flux-challenge-reagent.current-planet :as current-planet]
              [flux-challenge-reagent.sith-lord :as sith-lord]))

(defonce state (r/atom (vec (repeat 5 nil)) :validator vector?))

(defn some-item? [pred]
  (some pred @state))

(defn- assoc-item [items index item]
  (if (and (util/is-valid-index? index items) (nil? (get items index)))
    (assoc items index item)
    items))

(defn- accept-item [items item]
  (let [item-index (util/index-of (map sith-lord/id items) (sith-lord/id item))]
    (-> items
        (assoc item-index item)
        (assoc-item (inc item-index) (sith-lord/next-item item))
        (assoc-item (dec item-index) (sith-lord/prev-item item)))))

(defn- manage-requests! [items current-planet]
  (let [frozen (some (sith-lord/homeworld-matches? current-planet) items)
        response-handler (fn [item] (swap! state #(-> %
                                                      (accept-item item)
                                                      (manage-requests! @current-planet/state))))
        f (if frozen
            sith-lord/abort-request!
            (sith-lord/start-request! response-handler))]
    (vec (map f items))))

(defn- scroll-once! [items direction]
  (case direction
    :up (let [first-item (first items)
              prev-item (sith-lord/prev-item first-item)]
          (sith-lord/abort-request! (last items))
          (vec (cons prev-item (pop items))))
    :down (let [last-item (last items)
                next-item (sith-lord/next-item last-item)]
            (sith-lord/abort-request! (first items))
            (conj (vec (rest items)) next-item))))

(defn scroll! [n direction]
  {:pre [(> n 0)
         (some #{direction} '(:up :down))]}
  (swap! state (fn [items]
                (reduce #(scroll-once! %1 direction) items (range n)))))

(defn init! [first-item-id]
  (swap! state #(-> %
                    (assoc-item 0 (sith-lord/new-item first-item-id))
                    (manage-requests! @current-planet/state))))

(add-watch current-planet/state :current-planet-watcher
           (fn [key a old-planet new-planet] (swap! state manage-requests! new-planet)))
