(ns flux-challenge-reagent.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [reagent.core :as r]
              [flux-challenge-reagent.ws-client :as ws]
              [ajax.core :refer [GET]])
    (:import goog.History))

;; -------------------------
;; State
(defonce current-planet (r/atom {}))
(defonce sith-lords-list (r/atom []))

;; -------------------------
;; Views

(defn render-sith-lord [sith-lord]
  [:li {:class "css-slot" :key (get sith-lord "id")}
   [:h3 (get sith-lord "name")]
   [:h6 (str "Homeworld: " (get-in sith-lord ["homeworld" "name"]))]])

(defn home-page []
  [:div {:class "css-root"}
   [:h1 {:class "css-planet-monitor"} (str "Obi-Wan currently on " (get @current-planet "name"))]

   [:section {:class "css-scrollable-list"}
    [:ul {:class "css-slots"}
     (map render-sith-lord @sith-lords-list)]]

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
;; Sith Lords list
(defn handle-sith-lord-response [response]
  (swap! sith-lords-list conj response)
  (if (< (count @sith-lords-list) 5)
    (request-sith-lord (get-in response ["apprentice" "id"]))))

(defn request-sith-lord [id]
  (GET (str "http://localhost:3000/dark-jedis/" id)
       :response-format :json
       :handler handle-sith-lord-response))

(defn request-darth-sidious []
  (request-sith-lord "3616"))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root)
  (subscribe-to-current-planet!)
  (request-darth-sidious))
