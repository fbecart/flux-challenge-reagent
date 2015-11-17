(ns flux-challenge-reagent.scrollable-list)

(defprotocol Item
  (id [_])
  (next-item [_])
  (prev-item [_])
  (destroy [_]))

(extend-protocol Item
  nil
  (id [_] nil)
  (next-item [_] nil)
  (prev-item [_] nil)
  (destroy [_] nil))

(defn- is-valid-index? [index items]
  (and (>= index 0) (< index (count items))))

(defn- index-of [items item]
  (let [i (count (take-while #(not= item %) items))]
    (when (or (< i (count items))
            (= item (last items)))
      i)))

(defn- assoc-item [items index item]
  (if (and (is-valid-index? index items) (nil? (get items index)))
    (assoc items index item)
    items))

(defn accept-loaded-item [items item]
  (let [item-index (index-of (map id items) (id item))]
    (-> items
        (assoc item-index item)
        (assoc-item (inc item-index) (next-item item))
        (assoc-item (dec item-index) (prev-item item)))))

(defn- scroll-once! [items direction]
  (case direction
    :up (let [first-item (first items)
              prev-item (prev-item first-item)]
          (destroy (last items))
          (vec (cons prev-item (pop items))))
    :down (let [last-item (last items)
                next-item (next-item last-item)]
            (destroy (first items))
            (conj (vec (rest items)) next-item))))

(defn scroll! [items n direction]
  {:pre [(> n 0)
         (some #{direction} '(:up :down))]}
  (reduce #(scroll-once! %1 direction) items (range n)))

(defn new-list [size first-item]
  {:pre [(> size 0)]}
  (let [empty-items (vec (repeat size nil))]
    (assoc empty-items 0 first-item)))
