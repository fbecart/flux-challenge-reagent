(ns flux-challenge-reagent.ws-client
  (:require [cognitect.transit :as t]))

(def json-reader (t/reader :json))

(defn receive-transit-msg! [update-fn]
 (fn [msg]
   (update-fn
     (->> msg .-data (t/read json-reader)))))

(defn make-websocket! [url receive-handler]
 (println "Attempting to connect websocket")
 (if-let [chan (js/WebSocket. url)]
   (do
     (set! (.-onmessage chan) (receive-transit-msg! receive-handler))
     (println "Websocket connection established with: " url)
     chan)
   (throw (js/Error. "Websocket connection failed!"))))
