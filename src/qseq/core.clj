(ns qseq.core
  (:use qseq.util
        qseq.key
        qseq.clojureql)
  (:require [clojure.java.jdbc :as jdbc]
            [clojureql.core :as q]))

(defonce default-transactor (atom nil))

(defn transactor
  "construct a simple transactor, which runs a transaction on a connection from db"
  [db]
  (fn [fn]
    (jdbc/with-connection db
      (jdbc/transaction
        (fn)))))

(defmacro transaction
  "run some forms inside a transaction with the given transactor"
  [transactor & forms]
  `(~transactor (fn [] ~@forms)))

;;;;;;;;;;;;;;;;;;;;; bounded queries

(defn q-boundary-value
  "returns a query to find the bounding value of a (simple or compound) key from a query.
   key: a simple or compound key. defaults to the :key metadata on table or :id.
   operator: <, >, <=, >=. defaults to <=
   boundary: if given, returns bounding key value where (operator key boundary)

   if operator is < or <= then the maximum key value will be returned... if operator is > or >=
   then the minimum key value will be returned. sorts the results by key#desc for < and <=
   or by key#asc for > and >= and takes the key value from the first row"
  ([table & {:keys [key boundary operator]
             :or {key (sort-key table)
                  operator '<=}}]
     (-> table
         (q-inside-boundary operator key boundary)
         (q-sorted :key key :dir (boundary-query-sort-direction operator))
         (q/take 1)
         (q/pick key))))

(defn q-bounded
  "return a query bounded by the min/max value of a key, so
   it will return the same results even though rows are added to a table
   (assuming a monotonically increasing key value &c).

   given a query, returns a new query restricted to (operator key boundary).
   key: a simple or compound key. defaults to the :key metadata on table or :id.
   operator: <, >, <=, >=. defaults to <=
   boundary:  defaults to @(q-boundary-value table :key key :operator operator), and if no rows match that query
              then 'where false' is used as the condition"
  [table & {:keys [key boundary operator transactor]
            :or {key (sort-key table)
                 operator '<=
                 transactor @qseq.core/default-transactor}}]
  (let [use-boundary (or boundary (transaction transactor @(q-boundary-value table :key key :operator operator)))]
    (-> table
        ((fn [t] (if use-boundary
                  (q/select t (q/where (key-condition operator key use-boundary)))
                  (q/select t (q/where (= false))))))
        (with-meta {:key key}))))

;;;;;;;;;;;;;;;;;;;;;;; batched-sequences over queries

(defn qseq-batches
  "a lazy seq of batches of rows from a query.
   batches are sorted by key which defaults to the :key metadata item on table or :id,
   and traversal direction dir is either :asc or :desc"
  [table & {:keys [batch-size key dir lower-bound transactor]
            :or {batch-size 1000
                 key (sort-key table)
                 dir :asc
                 transactor @qseq.core/default-transactor}}]
  (if-not transactor
    (throw (RuntimeException. "no transactor!")))
  (lazy-seq
   (let [query (q-seq-batch table batch-size key dir lower-bound)
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
        (qseq-batches table :batch-size batch-size :key key :lower-bound max-key-value :dir dir :transactor transactor))))))

(defn qseq
  "a lazy seq of rows from a query.
   rows are fetched in batches of batch-size, which defaults to 1000.
   a key is used to sort the rows, which defaults to the :key metadata item on table or :id,
   and traversal direction is either :asc or :desc"
  ([table & {:keys [batch-size key dir transactor]
             :or {batch-size 1000
                  key (sort-key table)
                  dir :asc
                  transactor @qseq.core/default-transactor}}]
     (very-lazy-apply-concat nil (qseq-batches table :batch-size batch-size :key key :dir dir :transactor transactor))))
