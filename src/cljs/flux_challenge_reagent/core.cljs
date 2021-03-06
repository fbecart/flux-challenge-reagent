(ns flux-challenge-reagent.core
  (:require [reagent.core :as r]
            [flux-challenge-reagent.view :as view]
            [flux-challenge-reagent.current-planet :as current-planet]
            [flux-challenge-reagent.sith-lords :as sith-lords]))

(enable-console-print!)

(defn mount-root
  "Initializes the Reagent view."
  []
  (r/render-component [view/dashboard] (.getElementById js/document "app")))

(defn init!
  "Entry point of the application. Initializes its various components."
  []
  (mount-root)
  (current-planet/init-websocket-connection!)
  (sith-lords/init!))
