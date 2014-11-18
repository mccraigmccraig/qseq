(ns qseq.core
  "a lazy sequence over a Korma or ClojureQL query"
  (:use qseq.util
        qseq.key
        qseq.dispatch
        qseq.impl)
  (:require [clojure.java.jdbc :as jdbc]
            [korma.db :as kdb]
            clojure.java.jdbc.deprecated))

(def ^:dynamic *default-transactor*
  "if a *default-transactor* is set it will be used in calls where a transactor is required
   but not explicitly given"
  (atom nil))

(defn set-default-transactor
  "set a global value for *default-transactor*. may be dynamically rebound by
   with-default-transactor"
  [transactor]
  (swap! *default-transactor* (fn [old] transactor)))

(defn with-default-transactor-fn
  [transactor fn]
  (binding [*default-transactor* (atom transactor)]
    (fn)))

(defmacro with-default-transactor
  "execute forms with *default-transactor* bound to transactor"
  [transactor & forms]
  `(with-default-transactor-fn ~transactor (fn [] ~@forms)))

(defmacro transaction
  "run some forms inside a transaction with the given transactor"
  [transactor & forms]
  `(~transactor (fn [] ~@forms)))

(defn jdbc-only-transactor
  "construct a transactor, which runs a transaction on a connection from db"
  [db]
  (fn [f]
    (jdbc/db-transaction* db (fn [_] (f)))))

(defn transactor
  "transactor which binds the same default connection for korma, clojure.java.jdbc
   and clojure.java.jdbc.deprecated"
  [db]
  (fn [f]
    (kdb/with-db db
      ;; this function 'owns' the transaction
      (kdb/transaction
       ;; while the deprecated transaction fn uses the same :level and :rollback keys
       ;; in the *db* map as java.jdbc and korma in *current-conn*
       (binding [clojure.java.jdbc.deprecated/*db* kdb/*current-conn*]
          (f))))))

;;;;;;;;;;;;;;;;;;;;; bounded queries

(defn q-boundary-value
  "returns a query to find the boundary value of a (simple or compound) key from a query.

   key - a simple or compound key. defaults to the entity :pk (Korma) or :key metadata on query (ClojureQL) or :id.
   operator - <, >, <=, >=. defaults to <=
   boundary - if given, returns bounding key value where (operator key boundary)

   if operator is < or <= then the maximum key value will be returned... if operator is > or >=
   then the minimum key value will be returned. sorts the results by key#desc for < and <=
   or by key#asc for > and >= and takes the key value from the first row"
  ([query & {:keys [key boundary operator]
             :or {key (sort-key query)
                  operator '<=}}]
     (-> query
         (q-inside-boundary operator key boundary)
         (q-sorted :key key :dir (boundary-query-sort-direction operator))
         (q-limited 1))))

(defn q-bounded
  "return a query bounded by the min/max value of a key, so
   it will return the same results even though rows are added to a table
   (assuming a monotonically increasing key value &c).

   given a query, returns a new query restricted to (operator key boundary).
   key - a simple or compound key. defaults to the entity :pk (Korma) or :key metadata on query (ClojureQL) or :id.
   operator - <, >, <=, >=. defaults to <=
   boundary -  defaults to (q-boundary-value table :key key :operator operator), and if no rows match that query
              then 'where false' is used as the condition
   transactor - used to fetch each batch in it's own transaction. default *default-transactor*"
  [query & {:keys [key boundary operator transactor]
            :or {key (sort-key query)
                 operator '<=
                 transactor @*default-transactor*}}]
  (let [use-boundary (or boundary (pick (transaction transactor (execute (q-boundary-value query :key key :operator operator)))))]
    (-> query
        ((fn [q] (if use-boundary
                  (q-inside-boundary q operator key use-boundary)
                  (q-empty q))))
        (with-meta {:key key}))))

;;;;;;;;;;;;;;;;;;;;;;; batched-sequences over queries


(defn qseq-batches
  "a lazy seq of batches of rows from a query. results are sorted by a key and traversal direction
   can be given

   query - the Korma or ClojureQL query
   batch-size - batch size. default 1000
   key - simple or compound key to sort results. defaults to the entity :pk (Korma) or :key metadata on query (ClojureQL) or :id
   dir - :asc or :desc. default :asc
   lower-boundary - key value forming lower-boundary of results. default nil
   transactor - used to fetch each batch in it's own transaction. default *default-transactor*"
  [query & {:keys [batch-size key dir lower-boundary transactor]
            :or {batch-size 1000
                 key (sort-key query)
                 dir :asc
                 transactor @*default-transactor*}}]
  (if-not transactor
    (throw (RuntimeException. "no transactor!")))
  (lazy-seq
   (let [q (q-seq-batch query batch-size key dir lower-boundary)
         batch (transaction transactor (execute q))
         c (count batch)
         last-record (last batch)
         max-key-value (when last-record (if (sequential? key)
                                           (map last-record key)
                                           (last-record key)))
         _ (when (and last-record
                      (if (sequential? max-key-value)
                        (some nil? max-key-value)
                        (nil? max-key-value)))
             (throw (RuntimeException. (str "a component of the extracted key is nil: " (prn-str max-key-value)))))]
     (cons
      batch
      (if (= c batch-size) ;; if c<batch-size there are no more records
        (qseq-batches query :batch-size batch-size :key key :lower-boundary max-key-value :dir dir :transactor transactor))))))

(defn qseq
  "a lazy seq of rows from a query. results are sorted by a key, and traversal direction can be given. behind the
   scenes rows are fetched in batches, for efficiency

   query - the Korma or ClojureQL query
   batch-size - batch size. default 1000
   key - simple or compound key to sort results. defaults to the entity :pk (Korma) or :key metadata on query (ClojureQL) or :id
   dir - :asc or :desc. default :asc
   lower-boundary - key value forming lower-boundary of results. default nil
   transactor - used to fetch each batch in it's own transaction. default qseq.core/*default-transactor*"
  ([query & {:keys [batch-size key dir transactor]
             :or {batch-size 1000
                  key (sort-key query)
                  dir :asc
                  transactor @*default-transactor*}}]
     (very-lazy-apply-concat nil (qseq-batches query :batch-size batch-size :key key :dir dir :transactor transactor))))
