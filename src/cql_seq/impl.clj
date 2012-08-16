(ns cql-seq.impl
  (:use clojure.core.strint)
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [clojureql.core :as q]))

(def sql-bound-fns
  {:<= :max
   :>= :min})

(defn sql-bound-fn
  "given an inequality operator, return the sql function which can be used
   with a key to determine a default value for the bound"
  [operator]
  (let [sqlfn (sql-bound-fns operator)]
    (if-not sqlfn
      (throw (RuntimeException. (<< "~{operator} has no registered SQL function to determine a default bound"))))
    sqlfn))

(defn sort-key
  "default sort key for a query, either gotten from the :key metadata or defaults to :id"
  [table]
  (or (:key (meta table)) :id))

(defn simple-key-transform-col
  ""
  [sqlfn key]
  (let [sqlfnname (name sqlfn)
        keyname (name key)]
    [(<< "~{sqlfnname}(~{keyname})") :as (-> (str sqlfnname "_" keyname) keyword)]))

(defn key-transform-cols
  [sqlfn key]
  (if (sequential? key)
    (map (partial simple-key-transform-col sqlfn) key)
    (simple-key-transform-col sqlfn key)))

(defn compound-key-conditions
  [operator  eq-conds [key & next-keys] [bound & next-bounds]]
  (cons
   `(~'and ~@eq-conds (~(-> operator name symbol) ~key ~bound))
   (if (not-empty next-keys)
     (compound-key-conditions operator (conj eq-conds `(~'= ~key ~bound)) next-keys next-bounds))))

(defn key-condition
  "given an operator and a simple or compound key and corresponding bound, return query conditions for records
   which meet the predicate composed of the operator and bound"
  [operator key bound]
  (let [keys (if (sequential? key) key [key])
        bounds (if (sequential? bound) bound [bound])]
    (if (not= (count keys) (count bounds))
      (throw (RuntimeException. "key and upper-bound must have the same number of components")))
    (let [kc (compound-key-conditions operator nil keys bounds)]
      `(~'or ~@kc))))

(defn very-lazy-concat
  "lazier than (apply concat seqs)"
  [coll colls]
  (lazy-seq
    (cond
      (and (empty? coll) (empty? colls)) nil
      (empty? coll) (very-lazy-concat (first colls) (next colls))
      true (cons (first coll) (very-lazy-concat (next coll) colls)))))
