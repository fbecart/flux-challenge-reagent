(ns flux-challenge-reagent.core
    (:require [reagent.core :as r]
              [flux-challenge-reagent.current-planet :as current-planet]
              [flux-challenge-reagent.sith-lords :as sith-lords]))

;; -------------------------
;; Views
(defn render-sith-lord [sith-lord]
  (if sith-lord
    [:li.css-slot
     (if (= (get sith-lord "homeworld") @current-planet/state)
       {:class "current-planet-match"}
       {})
     [:h3 (get sith-lord "name")]
     [:h6 (str "Homeworld: " (get-in sith-lord ["homeworld" "name"]))]]
    [:li.css-slot]))

(defn home-page []
  [:div.css-root
   [:h1.css-planet-monitor (str "Obi-Wan currently on " (get @current-planet/state "name"))]

   [:section.css-scrollable-list
    [:ul.css-slots
     (doall (map-indexed
             #(with-meta (render-sith-lord %2) {:key %1})
             @sith-lords/coll))]]

   [:div.css-scroll-buttons
    [:button.css-button-up
     {:on-click #(sith-lords/scroll! 2 :up)}]
    [:button.css-button-down
     {:on-click #(sith-lords/scroll! 2 :down)}]]])

;; -------------------------
;; Initialize app
(defn init! []
  (r/render-component [home-page] (.getElementById js/document "app"))
  (current-planet/init-websocket-connection!)
  (sith-lords/init!))
