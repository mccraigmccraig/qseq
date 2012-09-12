(ns qseq.clojureql
  "ClojureQL specific implementation"
  (:use qseq.util
        qseq.key)
  (:require [clojureql.core :as q]))

(defn sort-key
  "default sort key for a query, either gotten from the :key metadata or defaults to :id"
  [table]
  (or (:key (meta table)) :id))

(defn eval-where
  "well. this is what you get for using macros in your api"
  [conds]
  (eval
   `(clojureql.core/where ~conds)))

(defn q-empty
  "return a query which will match no rows"
  [table]
  (q/select table (q/where (= false))))

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

(defn simple-key-sort
  "given a :key return a :key#desc descending sort specifiers"
  [key & {:keys [dir] :or {dir "asc"}}]
  (-> (str (name key) "#" (name dir))
      keyword))

(defn key-sort
  "given a simple or compound key, return a list of descending sort specifiers"
  [key & {:keys [dir] :or {dir "asc"}}]
  (map (fn [k] (simple-key-sort k :dir dir))
       (make-sequential key)))

(defn q-sorted
  "returns a query sorted by key in direction dir. key may be simple or compound.
   if key is not supplied, defaults to the :key metadata on table or :id
   if dir is not supplied, defaults to asc"
  [table & {:keys [key dir] :or {key (sort-key table) dir "asc"}}]
  (-> table
      (q/sort (key-sort key :dir dir))
      (with-meta {:key key})))

(defn q-limited
  "returns a query with limit"
  [query limit]
  (q/take query limit))

(defn execute
  "execute a query"
  [query]
  @query)
