(ns flux-challenge-reagent.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [reagent.core :as r]
              [flux-challenge-reagent.ws-client :as ws]
              [flux-challenge-reagent.http-client :as http])
    (:import goog.History))

;; -------------------------
;; Constant parameters
(def list-size 5)

;; -------------------------
;; State
(defonce current-planet (r/atom {}))
(defonce sith-lords-list (r/atom (vec (repeat list-size nil))))

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
    [:button.css-button-down]]])

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
(defn nil-head-count [v]
  (count (take-while nil? v)))

(defn add-next-sith-lord! [l sith-lord]
  (let [index-of-nil-tail (- list-size (nil-head-count (reverse l)))]
    (assoc l index-of-nil-tail sith-lord)))

(defn handle-next-sith-lord! [sith-lord]
  (swap! sith-lords-list add-next-sith-lord! sith-lord)
  (if (nil? (peek @sith-lords-list))
    (if-let [apprentice-url (get-in sith-lord ["apprentice" "url"])]
      (http/get-json apprentice-url handle-next-sith-lord!))))

(defn request-darth-sidious []
  (http/get-json "http://localhost:3000/dark-jedis/3616" handle-next-sith-lord!))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root)
  (subscribe-to-current-planet!)
  (request-darth-sidious))
