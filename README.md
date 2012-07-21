# cql-seq

A library which provides a sequence abstraction for use with ClojureQL queries

## Usage

  (require '[clojureql.core :as q])
  (require '[cql-seq.core :as qs])

  (-> (q/table :users)
      qs/q-seq
      first)

## License

Copyright © 2012 mccraigmccraig of the clan mccraig

Distributed under the Eclipse Public License, the same as Clojure.
