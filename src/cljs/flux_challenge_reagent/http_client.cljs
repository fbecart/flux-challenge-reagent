(ns flux-challenge-reagent.http-client
    (:require [ajax.core :refer [GET]]))

(defn get-json [url handler]
  (GET url :response-format :json :handler handler))
