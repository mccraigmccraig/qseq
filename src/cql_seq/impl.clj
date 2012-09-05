(ns cql-seq.impl
  (:use clojure.core.strint)
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [clojureql.core :as q]))

(def boundary-query-sort-directions
  "sort directions for querying boundary values under given operators"
  {"<"  :desc
   "<=" :desc
   ">"  :asc
   ">=" :asc})

(defn boundary-query-sort-direction
  "return the sort direction to use such that the first row of a bounded query will
   return the boundary value of the key under the operator"
  [operator]
  (boundary-query-sort-directions (name operator)))

(def exclusion-operators-for-traversal-directions
  "exclusion operators for different sort directions, used to exclude seen results"
  {"asc" '>
   "desc" '<})

(defn exclusion-operator-for-traversal-direction
  "give a sort direction return the exclusion operator used to exclude previously seen results"
  [dir]
  (exclusion-operators-for-traversal-directions (name dir)))

(defn very-lazy-apply-concat
  "lazier than (apply concat seqs)... evaluates nothing at construction time"
  [coll colls]
  (lazy-seq
    (cond
      (and (empty? coll) (empty? colls)) nil
      (empty? coll) (very-lazy-apply-concat (first colls) (next colls))
      true (cons (first coll) (very-lazy-apply-concat (next coll) colls)))))

(defn sort-key
  "default sort key for a query, either gotten from the :key metadata or defaults to :id"
  [table]
  (or (:key (meta table)) :id))

(defn simple-key-sort
  "given a :key return a :key#desc descending sort specifiers"
  [key & {:keys [dir] :or {dir "asc"}}]
  (-> (str (name key) "#" (name dir))
      keyword))

(defn key-sort
  "given a simple or compound key, return a list of descending sort specifiers"
  [key & {:keys [dir] :or {dir "asc"}}]
  (map (fn [k] (simple-key-sort k :dir dir))
       (if (sequential? key) key [key])))

(defn compound-key-conditions
  "expand conditions restricting (operator key boundary). eq-conds accumulates equal conditions
   for compound keys"
  [operator eq-conds [key & next-keys] [boundary & next-boundaries]]
  (cons
   `(~'and ~@eq-conds (~(-> operator name symbol) ~key ~boundary))
   (if (not-empty next-keys)
     (compound-key-conditions operator (conj eq-conds `(~'= ~key ~boundary)) next-keys next-boundaries))))

(defn key-condition
  "given an operator and a simple or compound key and corresponding boundary, return query conditions for records
   which meet (key operator boundary)"
  [operator key boundary]
  (let [keys (if (sequential? key) key [key])
        boundaries (if (sequential? boundary) boundary [boundary])]
    (if (not= (count keys) (count boundaries))
      (throw (RuntimeException. "key and upper-bound must have the same number of components")))
    (let [kc (compound-key-conditions operator nil keys boundaries)]
      `(~'or ~@kc))))

(defn q-inside-boundary
  "returns a query bounded by (key operator boundary), for both simple and compound keys"
  [table operator key boundary]
  (-> table
      (fn [table] (if boundary
                   (q/select table (q/where (key-condition operator key boundary)))
                   table))))

(defn q-outside-boundary
  "returns a query bounded by (not (key operator boundary)), for both simple and compound keys"
  [table operator key boundary]
  (-> table
      (fn [table] (if boundary
                   (q/select table (q/where (not (key-condition operator key boundary))))
                   table))))

(defn q-sorted
  "returns a query sorted by key in direction dir. key may be simple or compound.
   if key is not supplied, defaults to the :key metadata on table or :id
   if dir is not supplied, defaults to asc"
  [table & {:keys [key dir] :or {key (sort-key table) dir "asc"}}]
  (-> table
      (q/sort (key-sort key :dir dir))
      (with-meta {:key key})))

(defn q-seq-batch
  "query retrieving a batch of records sorted by key with (not (operator key lower-bound))"
  [table batch-size key dir lower-boundary]
  (-> table
      (q-sorted :key key :dir dir)
      (q-outside-boundary (exclusion-operator-for-traversal-direction dir) key lower-boundary)
      (q/take batch-size)))
