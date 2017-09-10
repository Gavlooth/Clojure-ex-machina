# analytics

An early attempt of data exploration in clojure.
This repository is ment to evolve in a proof of concept of
how data exploitation can be done using clojure.
and clojurescript Clojure.
For now only plots the distribution of NAs as well as the Correlation between variables
of the cervical canser risk classification data-set from kaggle

The followint tools where used:
### Clojure:
* Mastodon c kixi.stats for the data exploration
* Clojure.spec for data validation/coercion
* clojure.data for csv manipulation
* kamel-case-kebab for proper csv variable name handling
* Apache commons StringUtils for efficient string manipulation

### ClojureScript:
* Dommy.js for dom element manipulation
* Plotly.js For visualization of the data
## Installation

Download and install leiningen then clonre this repository
## Usage

Run lein cljsbuild to build the clojurescript files
Start a repl with lein repl
run (start-server) to start a jetty server on localhost:12345
optionaly run (fg/start-figwheel!) to start the figwheel
file server and edit clojurescript realtime


<!-- FIXME: explanation -->

<!-- ## Options -->

<!-- FIXME: listing of options this app accepts. -->

<!-- ## Examples -->

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright© 2017

Distributed under the MIT Lisence
