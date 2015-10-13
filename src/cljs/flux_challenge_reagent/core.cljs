(ns flux-challenge-reagent.core
    (:require [reagent.core :as r]
              [flux-challenge-reagent.http-client :as http]
              [flux-challenge-reagent.util :as util]
              [flux-challenge-reagent.current-planet :as current-planet]))

;; -------------------------
;; Constant parameters
(def list-size 5)
(def scroll-range 2)
(def darth-sidious-url "http://localhost:3000/dark-jedis/3616")

;; -------------------------
;; State
(defonce sith-lords-list (r/atom (repeat list-size nil)))
(defonce next-apprentice-request (r/atom nil))
(defonce next-master-request (r/atom nil))

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
             @sith-lords-list))]]

   [:div.css-scroll-buttons
    [:button.css-button-up {:on-click scroll-up!}]
    [:button.css-button-down {:on-click scroll-down!}]]])

;; -------------------------
;; Sith Lords list
(defn handle-next-apprentice! [apprentice]
  (reset! next-apprentice-request nil)
  (swap! sith-lords-list
         #(util/reverse-seq-fn util/replace-nil-head-end % apprentice))
  (if (nil? (peek (vec @sith-lords-list)))
    (load-next-apprentice)))

(defn handle-next-master! [master]
  (reset! next-master-request nil)
  (swap! sith-lords-list util/replace-nil-head-end master)
  (if (nil? (first @sith-lords-list))
    (load-next-master)))

(defn request-sith-lord [request-atom url handler]
  (swap! request-atom
         (fn [current-request]
           (if (nil? current-request)
             (http/get-json url handler)
             current-request))))

(defn load-next-apprentice []
  (let [last-sith-lord (first (filter some? (reverse @sith-lords-list)))]
    (if-let [apprentice-url (get-in last-sith-lord ["apprentice" "url"])]
      (request-sith-lord next-apprentice-request apprentice-url handle-next-apprentice!))))

(defn load-next-master []
  (let [first-sith-lord (first (filter some? @sith-lords-list))
        master-url (get-in first-sith-lord ["master" "url"])]
    (request-sith-lord next-master-request master-url handle-next-master!)))

(defn scroll-down! []
  (swap! sith-lords-list util/slide scroll-range)
  (load-next-apprentice))

(defn scroll-up! []
  (swap! sith-lords-list #(util/reverse-seq-fn util/slide % scroll-range))
  (load-next-master))

;; -------------------------
;; Initialize app
(defn init! []
  (r/render-component [home-page] (.getElementById js/document "app"))
  (current-planet/init-websocket-connection!)
  (request-sith-lord next-apprentice-request darth-sidious-url handle-next-apprentice!))
