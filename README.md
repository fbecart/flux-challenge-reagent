## Reagent-based Flux Challenge implementation

This is an implementation of the [Flux Challenge](https://github.com/staltz/flux-challenge) using [ClojureScript](https://github.com/clojure/clojurescript) and [Reagent](https://reagent-project.github.io/).

### Set up

* Run the Flux Challenge server, as described in its [README](https://github.com/staltz/flux-challenge/blob/master/README.md) file
* Install [Leiningen](http://leiningen.org/)

### Run

### Development environment

* Run `lein figwheel`
* Open [http://localhost:3449](http://localhost:3449)

### Production environment

* Run `lein uberjar`
* Run `java -jar target/flux-challenge-reagent.jar`
* Open [http://localhost:3001](http://localhost:3001)
