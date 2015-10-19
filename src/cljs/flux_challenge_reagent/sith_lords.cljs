(ns flux-challenge-reagent.sith-lords
    (:require [reagent.core :as r]
              [ajax.core :as ajax]
              [flux-challenge-reagent.util :as util]
              [flux-challenge-reagent.current-planet :as current-planet]))

(defonce coll (r/atom (vec (repeat 5 nil)) :validator vector?))

(defn frozen? []
  (let [sith-lords-homeworlds (map #(get-in % [:sith-lord "homeworld"]) @coll)]
    (not (empty? (filter current-planet/is? sith-lords-homeworlds)))))

(defn sith-lord-url [id]
  (str "http://localhost:3000/dark-jedis/" id))

(defn sith-lord-item [sith-lord]
  {:id (get sith-lord "id")
   :sith-lord sith-lord})

(defn sith-lord-request-item! [id]
  (if id
    (let [url (sith-lord-url id)]
      {:id id
       :request (ajax/GET url
                          :response-format :json
                          :handler handle-sith-lord-response!)})
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

(defn abort-request! [item]
  (if-let [request (:request item)]
    (ajax/abort request)))

(defn scroll-once! [coll direction]
  (case direction
    :up (let [first-sith-lord (-> coll first :sith-lord)
              master-id (get-in first-sith-lord ["master" "id"])
              master-request (sith-lord-request-item! master-id)]
          (abort-request! (peek coll))
          (into [master-request] (pop coll)))
    :down (let [last-sith-lord (-> coll last :sith-lord)
                apprentice-id (get-in last-sith-lord ["apprentice" "id"])
                apprentice-request (sith-lord-request-item! apprentice-id)]
            (abort-request! (nth coll 0))
            (conj (subvec coll 1) apprentice-request))))

(defn scroll! [n direction]
  (swap! coll (fn [coll]
                (reduce #(scroll-once! %1 direction) coll (range n)))))

(defn init! []
  (let [darth-sidious-id 3616]
    (swap! coll #(request-sith-lord! % 0 darth-sidious-id))))
