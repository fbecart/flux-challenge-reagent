(ns flux-challenge-reagent.sith-lords
    (:require [reagent.core :as r]
              [ajax.core :as ajax]
              [flux-challenge-reagent.util :as util]
              [flux-challenge-reagent.current-planet :as current-planet]))

(defonce coll (r/atom (vec (repeat 5 nil)) :validator vector?))

(defn homeworld-matches? [coll current-planet]
  (let [sith-lords-homeworlds (map #(get-in % [:sith-lord "homeworld"]) coll)]
     (not (empty? (filter #(= % current-planet) sith-lords-homeworlds)))))

(defn frozen? []
  (homeworld-matches? @coll @current-planet/state))

(defn pending-sith-lord-item [id]
  (if id {:id id} nil))

(defn assoc-pending-sith-lord [coll index id]
  (if (and (>= index 0) (< index (count coll)) id (nil? (get coll index)))
    (assoc coll index (pending-sith-lord-item id))
    coll))

(defn abort-request! [item]
  (if-let [request (:request item)]
    (do
      (ajax/abort request)
      (dissoc item :request))
    item))

(defn start-request! [item]
  (if-let [id (:id item)]
    (if (not (or (contains? item :sith-lord) (contains? item :request)))
      (let [url (str "http://localhost:3000/dark-jedis/" id)
            request (ajax/GET url :response-format :json :handler handle-sith-lord-response!)]
        (assoc item :request request))
      item)
    item))

(defn manage-requests! [coll current-planet]
  (let [frozen (homeworld-matches? coll current-planet)
        f (if frozen abort-request! start-request!)]
    (vec (map f coll))))

(defn accept-sith-lord [coll sith-lord]
  (let [sith-lord-id (get sith-lord "id")
        sith-lord-index (util/index-of (map :id coll) sith-lord-id)]
    (-> (vec coll)
        (assoc sith-lord-index {:id (get sith-lord "id")
                                :sith-lord sith-lord})
        (assoc-pending-sith-lord (inc sith-lord-index) (get-in sith-lord ["apprentice" "id"]))
        (assoc-pending-sith-lord (dec sith-lord-index) (get-in sith-lord ["master" "id"])))))

(defn handle-sith-lord-response! [sith-lord]
  (swap! coll #(-> %
                   (accept-sith-lord sith-lord)
                   (manage-requests! @current-planet/state))))

(defn scroll-once! [coll direction]
  (case direction
    :up (let [first-sith-lord (-> coll first :sith-lord)
              master-id (get-in first-sith-lord ["master" "id"])
              master-item (pending-sith-lord-item master-id)]
          (abort-request! (peek coll))
          (into [master-item] (pop coll)))
    :down (let [last-sith-lord (-> coll last :sith-lord)
                apprentice-id (get-in last-sith-lord ["apprentice" "id"])
                apprentice-item (pending-sith-lord-item apprentice-id)]
            (abort-request! (nth coll 0))
            (conj (subvec coll 1) apprentice-item))))

(defn scroll! [n direction]
  (swap! coll (fn [coll]
                (reduce #(scroll-once! %1 direction) coll (range n)))))

(defn init! []
  (let [darth-sidious-id 3616]
    (swap! coll #(-> %
                     (assoc-pending-sith-lord 0 darth-sidious-id)
                     (manage-requests! @current-planet/state)))))

(add-watch current-planet/state :current-planet-watcher
           (fn [key a old-planet new-planet] (swap! coll manage-requests! new-planet)))
