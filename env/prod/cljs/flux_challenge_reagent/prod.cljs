(ns flux-challenge-reagent.prod
  (:require [flux-challenge-reagent.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
