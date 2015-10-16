(ns flux-challenge-reagent.core
  (:require [reagent.core :as r]
            [flux-challenge-reagent.view :as view]
            [flux-challenge-reagent.current-planet :as current-planet]
            [flux-challenge-reagent.sith-lords :as sith-lords]))

(defn mount-root []
  (r/render-component [view/dashboard] (.getElementById js/document "app")))

(defn init! []
  (mount-root)
  (current-planet/init-websocket-connection!)
  (sith-lords/init!))
