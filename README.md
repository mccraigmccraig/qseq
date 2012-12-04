# qseq

[![Build Status](https://secure.travis-ci.org/mccraigmccraig/qseq.png)](http://travis-ci.org/mccraigmccraig/qseq)

A library which provides a lazy sequence over Korma and ClojureQL queries

Results are sorted by a key, which may be a compound key. The direction of the sort may be specified

Results are fetched in batches for efficiency. The batch-size may be specified

A transactor function should be supplied, which will be called with a function which executes SQL requests. The
transactor should set up a transaction and call its parameter function. A function is supplied to construct a
transactor from a JDBC DataSource, and the `\*default-transactor\*` dynamic variable can be set for some forms for
the thread using the `with-default-transactor` macro, after which a transactor need not be supplied

## Usage

    (require '[clojureql.core :as q])
    (require '[korma.core :as k])
    (require '[qseq.core :as qs])

    (qs/with-default-transactor (qs/transactor my-data-source)

      (-> (q/table :users)
          qs/qseq
          first)

      (-> (k/select* :users)
          qs/qseq
          first)

      (-> (k/select* :users)
          (qs/qseq :batch-size 100 :key [:name :rank] :dir :desc :transactor my-transactor)
          first)

      (-> (q/table :users)
          (q/join (q/table :posts)
                  (q/where (= :posts.user_id :users.id)))
          (qs/qseq [:users.id :posts.id] :dir :asc)))

## Note

The current Korma release insists on managing it's own connections and connection-pooling, but a pull-request was accepted to fix this, so qseq currently depends on a snapshot build of Korma which permits
the use of an externally supplied connection. Qseq will move to a dependency on released Korma at the next release
been submitted

## License

Copyright © 2012 mccraigmccraig of the clan mccraig

Distributed under the Eclipse Public License, the same as Clojure.
