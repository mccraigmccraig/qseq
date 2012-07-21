(ns cql-seq.impl
  (:use clojure.core.strint)
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [clojureql.core :as q]))

(def sql-bound-fns
  "given an inequality operator, return the sql function which can be used
   with a key to determine a default value for the bound"
  {:<= :max
   :>= :min})

(defn sql-bound-fn
  [operator]
  (let [sqlfn (sql-bound-fns operator)]
    (if-not sqlfn
      (throw (RuntimeException. (<< "~{operator} has no registered SQL function to determine a default bound"))))
    sqlfn))

(defn sort-key
  "default sort key for a query, either gotten from the :key metadata or defaults to :id"
  [table]
  (or (:key (meta table)) :id))

(defn compound-key-condition
  [operator key bound]
  (if (not (sequential? bound)) (throw (RuntimeException. "if key is compound, upper-bound must be too")))
  (if (not= (count key) (count bound)) (throw (RuntimeException. "key and upper-bound must have the same length")))

  )

(defn simple-key-condition
  [operator key bound]
  (list (-> operator name symbol) key bound))

(defn key-condition
  [operator key bound]
  (let [condition-exprs (if (sequential? key)
                          (compound-key-condition operator key bound)
                          (simple-key-condition operator key bound))]
    (eval `(q/where ~condition-exprs))))

(defn simple-key-transform-col
  [sqlfn key]
  (let [sqlfnname (name sqlfn)
        keyname (name key)]
    [(<< "~{sqlfnname}(~{keyname})") :as (-> (str sqlfnname "_" keyname) keyword)]))

(defn key-transform-cols
  [sqlfn key]
  (if (sequential? key)
    (map (partial simple-key-transform-col sqlfn) key)
    (simple-key-transform-col sqlfn key)))

(defn very-lazy-concat
  "lazier than (apply concat seqs)"
  [coll colls]
  (lazy-seq
    (cond
      (and (empty? coll) (empty? colls)) nil
      (empty? coll) (very-lazy-concat (first colls) (next colls))
      true (cons (first coll) (very-lazy-concat (next coll) colls)))))
