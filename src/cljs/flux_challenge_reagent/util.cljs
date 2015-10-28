(ns flux-challenge-reagent.util)

(defn index-of [coll v]
  (let [i (count (take-while #(not= v %) coll))]
    (when (or (< i (count coll))
            (= v (last coll)))
      i)))

(defn is-valid-index? [i v]
  (and (>= i 0) (< i (count v))))
