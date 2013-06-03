(ns qseq.korma
  "Korma specific implementation"
  (:use qseq.key
        qseq.util)
  (:require [clojure.tools.logging :as log]
            [korma.core :as k]))

(defn sort-key
  "default sort-key for a query, either the :pk from the entity or defaults to :id"
  [query]
  (or (-> query meta :key) (-> query :ent :pk) :id))

(defn eval-where
  "blah blah macros"
  [query conds]
  (eval
   `(korma.core/where ~query ~conds)))

(defn q-empty
  "return a query which will match no rows"
  [query]
  (k/where query (= true false)))

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

(defn q-limited
  "returns a query with limit"
  [query limit]
  (k/limit query limit))

(defn execute
  "execute a query"
  [query]
  (log/debug (with-out-str (k/dry-run (k/exec query))))
  (k/exec query)
  )
