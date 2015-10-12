(ns flux-challenge-reagent.util)

(defn nil-head-count [v]
  (count (take-while nil? v)))

(defn slide [v n]
  (vec (concat (subvec v n) (repeat n nil))))
