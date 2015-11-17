(ns flux-challenge-reagent.sith-lord
  "Single item of the Sith Lords list. A Sith Lord is initialized with a simple ID.
  It will then be requested to obtain its data (so it can be displayed).
  The data of a Sith Lord contains:
  * its homeworld planet;
  * its preceding and following items (master and apprentice)."
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

(defn new-item
  "Returns a new SithLord with the provided ID."
  [id]
  (if id (map->SithLord {:id id}) nil))

(defn homeworld-matches?
  "Returns a function that returns true
  if the provided `SithLord` matches the provided planet."
  [planet]
  (fn [item]
    (= (get-in item [:data "homeworld"]) planet)))

(defn misses-rel?
  "Returns a function that returns true
  if the provided `SithLord` misses the provided relation."
  [rel]
  {:pre [(some #{rel} '("master" "apprentice"))]}
  (fn [item]
    (if-let [data (:data item)]
      (nil? (get-in data [rel "id"]))
      false)))

(defn abort-request!
  "If the provided `SithLord` is currently being requested,
  cancels the related request."
  [item]
  (if-let [request (:request item)]
    (do
      (ajax/abort request)
      (map->SithLord {:id (:id item)}))
    item))

(defn start-request!
  "Returns a function that will request the data of the provided `SithLord`
  using the provided callback response. The operation will be skipped
  if the `SithLord` has already been requested."
  [item-response-handler]
  (fn [item]
    (if-let [id (:id item)]
      (if (and (nil? (:request item)) (nil? (:data item)))
        (let [url (str "http://localhost:3000/dark-jedis/" id)
              json-response-handler #(item-response-handler (map->SithLord {:id id :data %}))
              request (ajax/GET url :response-format :json :handler json-response-handler)]
          (map->SithLord {:id id :request request}))
        item))))
