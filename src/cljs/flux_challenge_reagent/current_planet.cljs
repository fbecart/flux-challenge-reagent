(ns flux-challenge-reagent.current-planet
  "Holds the value of the current planet.
  The value is maintained by a persistent Websocket connection to the server."
  (:require [reagent.core :as r]
            [flux-challenge-reagent.ws-client :as ws]))

(defonce state (r/atom nil))

(defn is?
  "Returns true if the provided planet is equal to the current planet."
  [planet]
  (= planet @state))

(defn init-websocket-connection!
  "Initializes the socket connection to the server, keeping the state updated."
  []
  (ws/make-websocket! "ws://localhost:4000" #(reset! state %)))
