(ns cql-seq.core
  (:use clojure.core.strint
        cql-seq.impl)
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [clojureql.core :as q]))

(defonce db (atom nil))

(defn transactor
  "simple transactor, uses a transaction on the connection pool in db"
  [fn]
  (sql/with-connection @db
    (sql/transaction
     (fn))))

(defmacro transaction
  "run some forms inside a transaction with the given transactor"
  [transactor & forms]
  `(~transactor (fn [] ~@forms)))

(defn q-boundary-value
  "returns a query to find the boundary value of a (simple or compound) key from a clojureql query.
   key: a simple or compound key. defaults to the :key metadata on table or :id.
   operator: :<= or :>= . defaults to :<= .
             boundary value is determined by operator. :<= returns max(key), while :>= returns min(key).
   bound: if given, returns boundary value where (key operator bound)"
  ([table & {:keys [key bound operator] :or {key (sort-key table) operator :<=}}]
     (-> table
         ((fn [t] (if bound
                   (q/select t (key-condition operator key bound))
                    t)))
         (q/aggregate [(key-transform-cols (sql-bound-fn operator) key)])
         (q/pick key))))

(defn q-bounded
  "a stake in the sand. use it to construct a query bounded by the current value of a key, so
   it will return the same results even though rows are added to a table.

   given a clojureql query, returns a new clojureql query restricted to (key operator bound).
   key: a simple or compound key. defaults to the :key metadata on table or :id.
   operator: either :<= of :>= . defaults to :<=
   bound:  defaults to @(q-boundary-value table :key key :operator operator), and if no rows match that query
           then 'where false' is used as the condition"
  [table & {:keys [key bound operator transactor] :or {key (sort-key table) operator :<= transactor cql-seq.core/transactor}}]
  (let [use-bound (or bound (transaction transactor @(q-boundary-value table :key key :operator operator)))]
    (-> table
        ((fn [t] (if use-bound
                  (q/select t (key-condition operator key use-bound))
                  (q/select t (q/where (= false))))))
        (with-meta {:key key}))))

(defn q-sorted
  "given a clojureql query, returns a new clojureql query sorted by key.
   if key is not supplied, defaults to the :key metadata on table or :id"
  [table & {:keys [key] :or {key (sort-key table)}}]
  (-> table
      (q/sort [key])
      (with-meta {:key key})))

(defn q-seq-batch
  "query retrieving a batch of records sorted by key with key>lower-bound"
  [table batch-size key lower-bound]
  (-> table
      (q/sort [key])
      ((fn [table] (if lower-bound
                    (q/select table (key-condition :> key lower-bound))
                     table)))
      (q/take batch-size)))

(defn query-seq-batches
  "a lazy seq of batches of rows from a clojureql query.
   batches are sorted by key which defaults to the :key metadata item on table or :id"
  [table & {:keys [batch-size key lower-bound transactor] :or {batch-size 1000 key (sort-key table) transactor cql-seq.core/transactor}}]
  (lazy-seq
   (let [query (q-seq-batch table batch-size key lower-bound)
         batch (transaction transactor @query)
         c (count batch)
         last-record (last batch)
         max-key-value (if (sequential? key)
                         (map last-record key)
                         (last-record key))]
     (prn query)
     (cons
      batch
      (if (= c batch-size) ;; if c<batch-size there are no more records
        (query-seq-batches table :batch-size batch-size :key key :lower-bound max-key-value :transactor transactor))))))

(defn query-seq
  "a lazy seq of rows from a clojureql query.
   rows are fetched in batches of batch-size, which defaults to 1000.
   a key is used to sort the rows, which defaults to the :key metadata item on table or :id"
  ([table & {:keys [batch-size key transactor] :or {batch-size 1000 key (sort-key table transactor cql-seq.core/transactor)}}]
     (very-lazy-concat nil (query-seq-batches table :batch-size batch-size :key key :transactor transactor))))
