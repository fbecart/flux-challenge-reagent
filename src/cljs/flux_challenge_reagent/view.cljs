(ns flux-challenge-reagent.view
  (:require [flux-challenge-reagent.current-planet :as current-planet]
            [flux-challenge-reagent.sith-lords :as sith-lords]))

(defn sith-lord [item]
  (if-let [sith-lord (:sith-lord item)]
    [:li.css-slot
     (if (= (get sith-lord "homeworld") @current-planet/state)
       {:class "current-planet-match"}
       {})
     [:h3 (get sith-lord "name")]
     [:h6 (str "Homeworld: " (get-in sith-lord ["homeworld" "name"]))]]
    [:li.css-slot]))

(defn dashboard []
  [:div.css-root
   [:h1.css-planet-monitor (str "Obi-Wan currently on " (get @current-planet/state "name"))]

   [:section.css-scrollable-list
    [:ul.css-slots
     (doall (map-indexed
             #(with-meta (sith-lord %2) {:key %1})
             @sith-lords/coll))]]

   [:div.css-scroll-buttons
    [:button.css-button-up
     {:on-click #(sith-lords/scroll! 2 :up)}]
    [:button.css-button-down
     {:on-click #(sith-lords/scroll! 2 :down)}]]])
