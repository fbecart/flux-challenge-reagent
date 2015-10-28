(ns flux-challenge-reagent.view
  (:require [flux-challenge-reagent.current-planet :as current-planet]
            [flux-challenge-reagent.sith-lords :as sith-lords]
            [flux-challenge-reagent.sith-lord :as sith-lord]))

(defn sith-lord [sith-lord]
  (if sith-lord
    [:li.css-slot
     (if (current-planet/is? (get sith-lord "homeworld"))
       {:class "current-planet-match"}
       {})
     [:h3 (get sith-lord "name")]
     [:h6 (str "Homeworld: " (get-in sith-lord ["homeworld" "name"]))]]
    [:li.css-slot]))

(defn frozen? []
  (sith-lords/some-item? (sith-lord/homeworld-matches? @current-planet/state)))

(defn missing-rel? [rel]
  (sith-lords/some-item? (sith-lord/misses-rel? rel)))

(defn dashboard []
  [:div.css-root
   [:h1.css-planet-monitor (str "Obi-Wan currently on " (get @current-planet/state "name"))]

   [:section.css-scrollable-list
    [:ul.css-slots
     (doall (map-indexed
             #(with-meta (sith-lord (:data %2)) {:key %1})
             @sith-lords/state))]]

   [:div.css-scroll-buttons
    [:button.css-button-up
     (if (or (frozen?) (missing-rel? "master"))
       {:class "css-button-disabled"}
       {:on-click #(sith-lords/scroll! 2 :up)})]
    [:button.css-button-down
     (if (or (frozen?) (missing-rel? "apprentice"))
       {:class "css-button-disabled"}
       {:on-click #(sith-lords/scroll! 2 :down)})]]])
