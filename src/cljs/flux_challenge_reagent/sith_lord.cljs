(ns flux-challenge-reagent.sith-lord
    (:require [ajax.core :as ajax]
              [flux-challenge-reagent.scrollable-list :refer [Item]]))

(defrecord SithLord [id request data]
  Item
  (id [_] id)
  (next-item [_]
    (new-item (get-in data ["apprentice" "id"])))
  (prev-item [_]
    (new-item (get-in data ["master" "id"])))
  (destroy [this] (abort-request! this)))

(defn new-item [id]
  (if id (map->SithLord {:id id}) nil))

(defn homeworld-matches? [current-planet]
  (fn [item]
    (= (get-in item [:data "homeworld"]) current-planet)))

(defn misses-rel? [rel]
  {:pre [(some #{rel} '("master" "apprentice"))]}
  (fn [item]
    (if-let [data (:data item)]
      (nil? (get-in data [rel "id"]))
      false)))

(defn abort-request! [item]
  (if-let [request (:request item)]
    (do
      (ajax/abort request)
      (map->SithLord {:id (:id item)}))
    item))

(defn start-request! [item-response-handler]
  (fn [item]
    (if-let [id (:id item)]
      (if (and (nil? (:request item)) (nil? (:data item)))
        (let [url (str "http://localhost:3000/dark-jedis/" id)
              json-response-handler #(item-response-handler (map->SithLord {:id id :data %}))
              request (ajax/GET url :response-format :json :handler json-response-handler)]
          (map->SithLord {:id id :request request}))
        item))))
