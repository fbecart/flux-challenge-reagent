# Reagent-based Flux Challenge implementation

This is a full-featured implementation of the [Flux Challenge](https://github.com/staltz/flux-challenge). Please have a look at the project [README file](https://github.com/staltz/flux-challenge/blob/master/README.md) for a complete description of the requirements.

This solution uses [ClojureScript](https://github.com/clojure/clojurescript) and [Reagent](https://reagent-project.github.io/).

## Flux Challenge server

In order to see the UI work on your machine, you will want to run the server provided by the Flux Challenge. Its setup is described in the [README](https://github.com/staltz/flux-challenge/blob/master/README.md) file, but here is a short version:

### Setup

    git clone git@github.com:staltz/flux-challenge.git
    cd flux-challenge/server
    npm install

### Run

    npm start

## Project Setup

[Boot](http://boot-clj.com/) is our build tool. Installation instructions can be found [here](https://github.com/boot-clj/boot#install).

You'll also need to download the project sources:

    git clone git@github.com:fbecart/flux-challenge-reagent.git
    cd flux-challenge-reagent

## Run the UI

### Development environment

* Run `boot dev`
* Open [http://localhost:3449](http://localhost:3449)

### Production environment

* Run `boot production`
* Open [target/index.html](target/index.html)
