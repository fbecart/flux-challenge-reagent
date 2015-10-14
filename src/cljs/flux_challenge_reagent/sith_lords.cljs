(ns flux-challenge-reagent.sith-lords
    (:require [reagent.core :as r]
              [flux-challenge-reagent.http-client :as http]
              [flux-challenge-reagent.util :as util]))

;; -------------------------
;; Constant parameters
(def darth-sidious-id 3616)

;; -------------------------
;; State
(defonce coll (r/atom nil))

;; -------------------------
;; Sith Lords list
(defn sith-lord-url [id]
  (str "http://localhost:3000/dark-jedis/" id))

(defn sith-lord-item [sith-lord]
  {:id (get sith-lord "id")
   :sith-lord sith-lord})

(defn sith-lord-request-item! [id]
  (if id
    (let [url (sith-lord-url id)]
      {:id id
       :request (http/get-json url handle-sith-lord-response!)})
    nil))

(defn request-sith-lord! [coll index id]
  (if (and (>= index 0) (< index (count coll)) id (nil? (get coll index)))
    (assoc coll index (sith-lord-request-item! id))
    coll))

(defn accept-sith-lord! [coll sith-lord]
  (let [sith-lord-id (get sith-lord "id")
        sith-lord-index (util/index-of (map :id coll) sith-lord-id)]
    (-> (vec coll)
        (assoc sith-lord-index (sith-lord-item sith-lord))
        (request-sith-lord! (inc sith-lord-index) (get-in sith-lord ["apprentice" "id"]))
        (request-sith-lord! (dec sith-lord-index) (get-in sith-lord ["master" "id"])))))

(defn handle-sith-lord-response! [sith-lord]
  (swap! coll accept-sith-lord! sith-lord))

(defn scroll-once! [coll direction]
  (case direction
    :up (let [first-sith-lord (-> coll first :sith-lord)
              master-id (get-in first-sith-lord ["master" "id"])
              master-request (sith-lord-request-item! master-id)]
          (into [master-request] (pop coll)))
    :down (let [last-sith-lord (-> coll last :sith-lord)
                apprentice-id (get-in last-sith-lord ["apprentice" "id"])
                apprentice-request (sith-lord-request-item! apprentice-id)]
            (conj (subvec coll 1) apprentice-request))))

(defn scroll! [n direction]
  (swap! coll (fn [coll]
                (reduce #(scroll-once! %1 direction) coll (range n)))))

(defn init! []
  (reset! coll (request-sith-lord! (vec (repeat 5 nil)) 0 darth-sidious-id)))
