(ns qseq.korma
  (:use qseq.key
        qseq.util)
  (:require [korma.core :as k]))

(defn sort-key
  "default sort-key for a query, either the :pk from the entity or defaults to :id"
  [query]
  (or (-> query :ent :pk) :id))

(defn eval-where
  "blah blah macros"
  [query conds]
  (eval
   `(korma.core/where ~query ~conds)))

(defn q-inside-boundary
  "returns a query bounded by (key operator boundary), for both simple and compound keys"
  [query operator key boundary]
  (if boundary
    (eval-where query (key-condition operator key boundary))
    query))

(defn q-outside-boundary
  "returns a query bounded by (not (key operator boundary)), for both simple and compound keys"
  [query operator key boundary]
  (if boundary
    (eval-where query `(~'not ~(key-condition operator key boundary)))
    query))

(defn q-sorted
  "returns a query sorted by key in direction dir. key may be simple or compound.
   if key is not supplied, defaults to the :key metadata on query or :id
   if dir is not supplied, defaults to asc"
  [query & {:keys [key dir] :or {key (sort-key query) dir "asc"}}]
  (reduce (fn [q k]
            (k/order q k dir))
          query
          (make-sequential key)))

(defn q-seq-batch
  "query retrieving a batch of records sorted by key with (not (operator key lower-boundary))"
  [query batch-size key dir lower-boundary]
  (-> query
      (q-sorted :key key :dir dir)
      (q-outside-boundary (inclusion-operator-for-traversal-direction dir) key lower-boundary)
      (k/limit batch-size)))
