(ns flux-challenge-reagent.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [reagent.core :as r]
              [flux-challenge-reagent.ws-client :as ws])
    (:import goog.History))

;; -------------------------
;; State
(defonce current-planet (r/atom {}))

;; -------------------------
;; Views

(defn home-page []
  [:div {:class "css-root"}
   [:h1 {:class "css-planet-monitor"} (str "Obi-Wan currently on " (get @current-planet "name"))]

   [:section {:class "css-scrollable-list"}
    [:ul {:class "css-slots"}
     [:li {:class "css-slot"}
      [:h3 "Jorak Uln"]
      [:h6 "Homeworld: Korriban"]]
     [:li {:class "css-slot"}
      [:h3 "Skere Kaan"]
      [:h6 "Homeworld: Coruscant"]]
     [:li {:class "css-slot"}
      [:h3 "Na'daz"]
      [:h6 "Homeworld: Ryloth"]]
     [:li {:class "css-slot"}
      [:h3 "Kas'im"]
      [:h6 "Homeworld: Nal Hutta"]]
     [:li {:class "css-slot"}
      [:h3 "Darth Bane"]
      [:h6 "Homeworld: Apatros"]]]]

   [:div {:class "css-scroll-buttons"}
    [:button {:class "css-button-up"}]
    [:button {:class "css-button-down"}]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Websocket
(defn update-current-planet! [message]
  (reset! current-planet message))

(defn subscribe-to-current-planet! []
  (ws/make-websocket! "ws://localhost:4000" update-current-planet!))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root)
  (subscribe-to-current-planet!))
