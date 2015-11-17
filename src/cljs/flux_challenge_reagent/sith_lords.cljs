(ns flux-challenge-reagent.sith-lords
    (:require [reagent.core :as r]
              [flux-challenge-reagent.scrollable-list :as scrollable-list]
              [flux-challenge-reagent.current-planet :as current-planet]
              [flux-challenge-reagent.sith-lord :as sith-lord]))

(defonce state (r/atom nil))

(defn some-item? [pred]
  (some pred @state))

(defn- manage-requests! [items current-planet]
  (let [frozen (some (sith-lord/homeworld-matches? current-planet) items)
        response-handler (fn [item] (swap! state #(-> %
                                                      (scrollable-list/accept-loaded-item item)
                                                      (manage-requests! @current-planet/state))))
        f (if frozen sith-lord/abort-request! (sith-lord/start-request! response-handler))]
    (vec (map f items))))

(defn scroll! [n direction]
  (swap! state #(scrollable-list/scroll! % n direction)))

(defn init! []
  (let [darth-sidious (sith-lord/new-item 3616)
        items (scrollable-list/new-list 5 darth-sidious)]
    (reset! state (manage-requests! items @current-planet/state))))

(add-watch current-planet/state :current-planet-watcher
           (fn [key a old-planet new-planet] (swap! state manage-requests! new-planet)))
