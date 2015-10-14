(ns flux-challenge-reagent.sith-lords
    (:require [reagent.core :as r]
              [flux-challenge-reagent.http-client :as http]
              [flux-challenge-reagent.util :as util]))

;; -------------------------
;; Constant parameters
(def darth-sidious-url "http://localhost:3000/dark-jedis/3616")

;; -------------------------
;; State
(defonce coll (r/atom (repeat 5 nil)))
(defonce next-apprentice-request (r/atom nil))
(defonce next-master-request (r/atom nil))

;; -------------------------
;; Sith Lords list
(defn handle-next-apprentice! [apprentice]
  (reset! next-apprentice-request nil)
  (swap! coll
         #(util/reverse-seq-fn util/replace-nil-head-end % apprentice))
  (if (nil? (peek (vec @coll)))
    (load-next-apprentice)))

(defn handle-next-master! [master]
  (reset! next-master-request nil)
  (swap! coll util/replace-nil-head-end master)
  (if (nil? (first @coll))
    (load-next-master)))

(defn request-sith-lord [request-atom url handler]
  (swap! request-atom
         (fn [current-request]
           (if (nil? current-request)
             (http/get-json url handler)
             current-request))))

(defn load-next-apprentice []
  (let [last-sith-lord (first (filter some? (reverse @coll)))]
    (if-let [apprentice-url (get-in last-sith-lord ["apprentice" "url"])]
      (request-sith-lord next-apprentice-request apprentice-url handle-next-apprentice!))))

(defn load-next-master []
  (let [first-sith-lord (first (filter some? @coll))
        master-url (get-in first-sith-lord ["master" "url"])]
    (request-sith-lord next-master-request master-url handle-next-master!)))

(defn scroll! [n direction]
  (case direction
    :up (do
          (swap! coll #(util/reverse-seq-fn util/slide % n))
          (load-next-master))
    :down (do
            (swap! coll util/slide n)
            (load-next-apprentice))))

(defn init! []
  (request-sith-lord next-apprentice-request darth-sidious-url handle-next-apprentice!))
