(ns flux-challenge-reagent.server
  (:require [flux-challenge-reagent.handler :refer [app]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (env :port) "3001"))]
     (run-jetty app {:port port :join? false})))
