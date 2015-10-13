(ns flux-challenge-reagent.current-planet
    (:require [reagent.core :as r]
              [flux-challenge-reagent.ws-client :as ws]))

(defonce state (r/atom {}))

(defn init-websocket-connection! []
  (ws/make-websocket! "ws://localhost:4000" #(reset! state %)))
