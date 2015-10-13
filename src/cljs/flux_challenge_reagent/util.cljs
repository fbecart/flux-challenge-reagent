(ns flux-challenge-reagent.util)

(defn nil-head-count [v]
  (count (take-while nil? v)))

(defn slide [v n]
  (vec (concat (subvec v n) (repeat n nil))))

(defn reverse-vec-fn [f v & params]
  (vec (reverse (apply f (conj params (vec (reverse v)))))))

(defn replace-nil-head-end [coll x]
  (let [nil-head-end-index (- (nil-head-count coll) 1)]
    (assoc coll nil-head-end-index x)))
