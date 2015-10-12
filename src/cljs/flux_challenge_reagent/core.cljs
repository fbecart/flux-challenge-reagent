(ns flux-challenge-reagent.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [reagent.core :as r]
              [flux-challenge-reagent.ws-client :as ws]
              [flux-challenge-reagent.http-client :as http]
              [flux-challenge-reagent.util :as util])
    (:import goog.History))

;; -------------------------
;; Constant parameters
(def list-size 5)
(def scroll-range 2)
(def darth-sidious-url "http://localhost:3000/dark-jedis/3616")

;; -------------------------
;; State
(defonce current-planet (r/atom {}))
(defonce sith-lords-list (r/atom (vec (repeat list-size nil))))
(defonce next-apprentice-request (r/atom nil))

;; -------------------------
;; Views

(defn render-sith-lord [sith-lord]
  [:li.css-slot
   (if (some? sith-lord)
     (list [:h3 (get sith-lord "name")]
           [:h6 (str "Homeworld: " (get-in sith-lord ["homeworld" "name"]))]))])

(defn home-page []
  [:div.css-root
   [:h1.css-planet-monitor (str "Obi-Wan currently on " (get @current-planet "name"))]

   [:section.css-scrollable-list
    [:ul.css-slots
     (map render-sith-lord @sith-lords-list)]]

   [:div.css-scroll-buttons
    [:button.css-button-up]
    [:button.css-button-down {:on-click scroll-down!}]]])

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
(defn add-apprentice [sith-lords-list apprentice]
  (let [index-of-nil-tail (- list-size (util/nil-head-count (reverse sith-lords-list)))]
    (assoc sith-lords-list index-of-nil-tail apprentice)))

(defn request-next-apprentice [url]
  (swap! next-apprentice-request
         (fn [current-request]
           (if (nil? current-request)
             (http/get-json url handle-next-apprentice!)
             current-request))))

(defn load-next-apprentice []
  (let [last-sith-lord (first (filter some? (reverse @sith-lords-list)))]
    (if-let [apprentice-url (get-in last-sith-lord ["apprentice" "url"])]
      (request-next-apprentice apprentice-url))))

(defn handle-next-apprentice! [apprentice]
  (reset! next-apprentice-request nil)
  (swap! sith-lords-list add-apprentice apprentice)
  (if (nil? (peek @sith-lords-list))
    (load-next-apprentice)))

(defn scroll-down! []
  (swap! sith-lords-list util/slide scroll-range)
  (load-next-apprentice))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root)
  (subscribe-to-current-planet!)
  (request-next-apprentice darth-sidious-url))
