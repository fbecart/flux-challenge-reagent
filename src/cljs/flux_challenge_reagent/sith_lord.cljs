(ns flux-challenge-reagent.sith-lord
    (:require [ajax.core :as ajax]))

(defn homeworld-matches? [current-planet]
  (fn [item]
    (= (get-in item [:data "homeworld"]) current-planet)))

(defn misses-rel? [rel]
  {:pre [(some #{rel} '("master" "apprentice"))]}
  (fn [item]
    (if-let [data (:data item)]
      (nil? (get-in data [rel "id"]))
      false)))

(defn new-item [id]
  (if id {:id id} nil))

(defn abort-request! [item]
  (if-let [request (:request item)]
    (do
      (ajax/abort request)
      (dissoc item :request))
    item))

(defn start-request! [item-response-handler]
  (fn [item]
    (if-let [id (:id item)]
      (if (not (some item '(:data :request)))
        (let [url (str "http://localhost:3000/dark-jedis/" id)
              json-response-handler #(item-response-handler {:id id :data %})
              request (ajax/GET url :response-format :json :handler json-response-handler)]
          (assoc item :request request))
        item)
      item)))

(defn prev-item [item]
  (new-item (get-in item [:data "master" "id"])))

(defn next-item [item]
  (new-item (get-in item [:data "apprentice" "id"])))

(defn id [item]
  (:id item))
