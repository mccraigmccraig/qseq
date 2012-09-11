(ns qseq.clojureql
  (:use qseq.key)
  (:require [clojureql.core :as q]))

(defn eval-where
  "well. this is what you get for using macros in your api"
  [conds]
  (eval
   `(clojureql.core/where ~conds)))

(defn q-inside-boundary
  "returns a query bounded by (key operator boundary), for both simple and compound keys"
  [table operator key boundary]
  (if boundary
    (q/select table (eval-where (key-condition operator key boundary)))
    table))

(defn q-outside-boundary
  "returns a query bounded by (not (key operator boundary)), for both simple and compound keys"
  [table operator key boundary]
  (if boundary
    (q/select table (eval-where `(~'not ~(key-condition operator key boundary))))
    table))

(defn q-sorted
  "returns a query sorted by key in direction dir. key may be simple or compound.
   if key is not supplied, defaults to the :key metadata on table or :id
   if dir is not supplied, defaults to asc"
  [table & {:keys [key dir] :or {key (sort-key table) dir "asc"}}]
  (-> table
      (q/sort (key-sort key :dir dir))
      (with-meta {:key key})))

(defn q-seq-batch
  "query retrieving a batch of records sorted by key with (not (operator key lower-boundary))"
  [table batch-size key dir lower-boundary]
  (-> table
      (q-sorted :key key :dir dir)
      (q-outside-boundary (inclusion-operator-for-traversal-direction dir) key lower-boundary)
      (q/take batch-size)))
