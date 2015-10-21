(ns flux-challenge-reagent.sith-lords
    (:require [reagent.core :as r]
              [ajax.core :as ajax]
              [flux-challenge-reagent.util :as util]
              [flux-challenge-reagent.current-planet :as current-planet]))

(defonce ^:private items (r/atom (vec (repeat 5 nil)) :validator vector?))

(defn- homeworld-matches? [items current-planet]
  (let [sith-lords-homeworlds (map #(get-in % [:sith-lord "homeworld"]) items)]
     (not (empty? (filter #(= % current-planet) sith-lords-homeworlds)))))

(defn frozen? []
  (homeworld-matches? @items @current-planet/state))

(defn any-misses? [rel]
  {:pre [(some #{rel} '("master" "apprentice"))]}
  (let [loaded-sith-lords (filter some? (map :sith-lord @items))]
    (not (empty? (filter nil? (map #(get-in % [rel "id"]) loaded-sith-lords))))))

(defn sith-lords []
  (map :sith-lord @items))

(defn- assoc-pending-sith-lord [items index id]
  (if (and (>= index 0) (< index (count items)) (nil? (get-in items [index :id])))
    (assoc items index {:id id})
    items))

(defn- abort-request! [item]
  (if-let [request (:request item)]
    (do
      (ajax/abort request)
      (dissoc item :request))
    item))

(defn- start-request! [item]
  (if-let [id (:id item)]
    (if (not (some item '(:sith-lord :request)))
      (let [url (str "http://localhost:3000/dark-jedis/" id)
            request (ajax/GET url :response-format :json :handler handle-sith-lord-response!)]
        (assoc item :request request))
      item)
    item))

(defn- manage-requests! [items current-planet]
  (let [frozen (homeworld-matches? items current-planet)
        f (if frozen abort-request! start-request!)]
    (vec (map f items))))

(defn- accept-sith-lord [items sith-lord]
  (let [sith-lord-id (get sith-lord "id")
        sith-lord-index (util/index-of (map :id items) sith-lord-id)]
    (-> (vec items)
        (assoc sith-lord-index {:id (get sith-lord "id") :sith-lord sith-lord})
        (assoc-pending-sith-lord (inc sith-lord-index) (get-in sith-lord ["apprentice" "id"]))
        (assoc-pending-sith-lord (dec sith-lord-index) (get-in sith-lord ["master" "id"])))))

(defn- handle-sith-lord-response! [sith-lord]
  (swap! items #(-> %
                   (accept-sith-lord sith-lord)
                   (manage-requests! @current-planet/state))))

(defn- scroll-once! [items direction]
  (case direction
    :up (let [first-sith-lord (-> items first :sith-lord)
              master-id (get-in first-sith-lord ["master" "id"])
              master-item {:id master-id}]
          (abort-request! (peek items))
          (into [master-item] (pop items)))
    :down (let [last-sith-lord (-> items last :sith-lord)
                apprentice-id (get-in last-sith-lord ["apprentice" "id"])
                apprentice-item {:id apprentice-id}]
            (abort-request! (nth items 0))
            (conj (subvec items 1) apprentice-item))))

(defn scroll! [n direction]
  {:pre [(> n 0)
         (some #{direction} '(:up :down))]}
  (swap! items (fn [items]
                (reduce #(scroll-once! %1 direction) items (range n)))))

(defn init! []
  (let [darth-sidious-id 3616]
    (swap! items #(-> %
                     (assoc-pending-sith-lord 0 darth-sidious-id)
                     (manage-requests! @current-planet/state)))))

(add-watch current-planet/state :current-planet-watcher
           (fn [key a old-planet new-planet] (swap! items manage-requests! new-planet)))
