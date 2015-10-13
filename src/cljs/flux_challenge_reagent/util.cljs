(ns flux-challenge-reagent.util)

(defn nil-head-count [coll]
  (count (take-while nil? coll)))

(defn slide [coll n]
  (subvec (vec (concat coll (repeat n nil))) n))

(defn reverse-seq-fn [f coll & params]
  (reverse (apply f (conj params (reverse coll)))))

(defn replace-nil-head-end [coll x]
  (let [nil-head-end-index (- (nil-head-count coll) 1)]
    (assoc (vec coll) nil-head-end-index x)))
