# Reagent-based Flux Challenge implementation

This is a full-featured implementation of the [Flux Challenge](https://github.com/staltz/flux-challenge). Please have a look at the challenge [README file](https://github.com/staltz/flux-challenge/blob/master/README.md) for a complete listing of the requirements.

This particular solution is based on [ClojureScript](https://github.com/clojure/clojurescript) and [Reagent](https://reagent-project.github.io/).

## Flux Challenge server

In order to see the UI work on your machine, you will want to run your own version of the Flux Challenge server. Its setup is described in the [README](https://github.com/staltz/flux-challenge/blob/master/README.md) file, but here is a short version:

### Setup

    git clone git@github.com:staltz/flux-challenge.git
    cd flux-challenge/server
    npm install

### Run

    npm start

## Project Setup

To build this project, you first need to install [Boot](http://boot-clj.com/). Installation instructions can be found [here](https://github.com/boot-clj/boot#install).

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
