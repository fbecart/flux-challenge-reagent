(ns flux-challenge-reagent.scrollable-list
  "Eases the creation and the management of the ad hoc lazy scrollable list.")

(defprotocol Item
  "Protocol to implement for items of a scrollable list."
  (id [_] "ID of this item")
  (next-item [_] "Item following this one")
  (prev-item [_] "Item preceding this one")
  (destroy [_] "Function to call when this item is removed from the list"))

(extend-protocol Item
  nil
  (id [_] nil)
  (next-item [_] nil)
  (prev-item [_] nil)
  (destroy [_] nil))

(defn- is-valid-index?
  "Returns true if the index exists in the provided vector."
  [index items]
  (and (>= index 0) (< index (count items))))

(defn- index-of
  "Returns the index of the provided item in the provided sequence of items.
  Returns `nil` if the item could not be found."
  [items item]
  (let [i (count (take-while #(not= item %) items))]
    (when (or (< i (count items)) (= item (last items)))
      i)))

(defn- assoc-item
  "Associates the item to the provided index of a vector.
  This transformation will be skipped if the provided index is out of the vector
  or if there is already an element at the provided index."
  [items index item]
  (if (and (is-valid-index? index items) (nil? (get items index)))
    (assoc items index item)
    items))

(defn accept-loaded-item
  "Replaces an existing item with its new loaded value,
  then initializes the following and preceding items if applicable."
  [items item]
  (let [item-index (index-of (map id items) (id item))]
    (-> items
        (assoc item-index item)
        (assoc-item (inc item-index) (next-item item))
        (assoc-item (dec item-index) (prev-item item)))))

(defn- scroll-once!
  "Scrolls the provided vector up or down, depending on the provided direction."
  [items direction]
  (case direction
    :up (let [first-item (first items)
              prev-item (prev-item first-item)]
          (destroy (last items))
          (vec (cons prev-item (pop items))))
    :down (let [last-item (last items)
                next-item (next-item last-item)]
            (destroy (first items))
            (conj (vec (rest items)) next-item))))

(defn scroll!
  "Scrolls n times the provided vector up or down."
  [items n direction]
  {:pre [(> n 0)
         (some #{direction} '(:up :down))]}
  (reduce #(scroll-once! %1 direction) items (range n)))

(defn new-list
  "Creates a new scrollable list of the provided size,
  starting with the provided first item."
  [size first-item]
  {:pre [(> size 0)]}
  (let [empty-items (vec (repeat size nil))]
    (assoc empty-items 0 first-item)))
