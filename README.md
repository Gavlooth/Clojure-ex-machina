# Clojure-ex-machina

An early attempt of data exploration in clojure.
This repository is meant to evolve in a proof of concept of
how data exploration can be done with clojure.
and clojurescript.
For now, only plots the distribution of NAs and the Correlation (Matrix) between variables
for the cervical canser risk classification data-set from kaggle

The following tools where used:
### Clojure:
* Mastodon c's kixi.stats for data exploration
* Clojure.spec for data validation/coercion
* clojure.data for csv manipulation
* kamel-case-kebab for proper csv variable name handling
* Apache commons StringUtils for efficient string manipulation

### ClojureScript:
* Plumatic/Dommy for dom element manipulation
* Plotly.js For visualization of the data
## Installation

Download and install leiningen then clone this repository
## Usage

1. From the root of the cloned project run lein cljsbuild to build the clojurescript files
2. Start a repl with lein repl
4. Development proccess now has integrated the excellent stuartsierra/component library So
   Run (go) to start the jetty server on 12345, or (reset) to reload changes.
5. optionaly run (fg/start-figwheel!) to start the figwheel
   file server and compile clojurescript realtime


<!-- FIXME: explanation -->

<!-- ## Options -->

<!-- FIXME: listing of options this app accepts. -->

<!-- ## Examples -->

...

<!-- ### Bugs -->


## License

Copyright© 2017 Christos Chatzifountas

Distributed under the MIT Lisence
