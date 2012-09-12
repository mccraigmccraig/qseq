(ns qseq.core-test
  (:use clojure.test
        midje.sweet
        qseq.impl
        qseq.dispatch
        qseq.core)
  (:require [clojure.string :as str]
            [clojureql.core :as q]
            [korma.core :as k]))


(defn cql-qstr
  [q]
  (str/trim (with-out-str (prn q))))

(defn k-qstr
  [q]
  (k/sql-only (k/exec q)))

(fact
  (cql-qstr (q-boundary-value (q/table :foo))) =>
  "SELECT foo.* FROM foo ORDER BY foo.id DESC LIMIT 1"

  (cql-qstr (q-boundary-value (q/table :foo) :key :bar)) =>
  "SELECT foo.* FROM foo ORDER BY foo.bar DESC LIMIT 1"

  (cql-qstr (q-boundary-value (q/table :foo) :key [:bar :baz])) =>
  "SELECT foo.* FROM foo ORDER BY foo.bar DESC,foo.baz DESC LIMIT 1"

  (cql-qstr (q-boundary-value (q/table :foo) :key [:bar :baz] :operator '>)) =>
  "SELECT foo.* FROM foo ORDER BY foo.bar ASC,foo.baz ASC LIMIT 1"

  (cql-qstr (q-boundary-value (q/table :foo) :boundary 10)) =>
  "SELECT foo.* FROM foo WHERE (((foo.id <= 10))) ORDER BY foo.id DESC LIMIT 1"

  (cql-qstr (q-boundary-value (q/table :foo) :key [:bar :baz] :operator '> :boundary [10 20])) =>
  "SELECT foo.* FROM foo WHERE (((foo.bar > 10)) OR ((foo.bar = 10) AND (foo.baz > 20))) ORDER BY foo.bar ASC,foo.baz ASC LIMIT 1"

  (k-qstr (q-boundary-value (k/select* :foo) :key [:bar :baz] :operator '> :boundary [10 20])) =>
  "SELECT \"foo\".* FROM \"foo\" WHERE ((\"foo\".\"bar\" > ?) OR (\"foo\".\"bar\" = ? AND \"foo\".\"baz\" > ?)) ORDER BY \"foo\".\"bar\" ASC, \"foo\".\"baz\" ASC LIMIT 1")

(fact
  (cql-qstr (q-bounded (q/table :foo) :boundary 10)) =>
  "SELECT foo.* FROM foo WHERE (((foo.id <= 10)))"

  (cql-qstr (q-bounded (q/table :foo) :key [:bar :baz] :boundary [10 20])) =>
  "SELECT foo.* FROM foo WHERE (((foo.bar <= 10)) OR ((foo.bar = 10) AND (foo.baz <= 20)))"

  (k-qstr (q-bounded (k/select* :foo) :key [:bar :baz] :boundary [10 20])) =>
  "SELECT \"foo\".* FROM \"foo\" WHERE ((\"foo\".\"bar\" <= ?) OR (\"foo\".\"bar\" = ? AND \"foo\".\"baz\" <= ?))")


(with-default-transactor (fn [f] (f))

  (fact
    (doall (qseq-batches (q/table :foo) :batch-size 2)) => [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] [{:id 5}]]
    (provided
      (execute anything) =streams=> [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] [{:id 5}]] ))

  (fact
    (doall (qseq-batches (q/table :foo) :batch-size 2)) => [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] []]
    (provided
      (execute anything) =streams=> [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] []] ))

  (fact
    (doall (qseq-batches (q/table :foo) :batch-size 2)) => [ [] ]
    (provided
      (execute anything) =streams=> [ [] ] ))

  )

(with-default-transactor (fn [f] (f))

  (fact
    (doall (qseq (q/table :foo) :batch-size 2)) => [ {:id 1} {:id 2} {:id 3} {:id 4} {:id 5} ]
    (provided
      (execute anything) =streams=> [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] [{:id 5}]] ))

  (fact
    (doall (qseq (q/table :foo) :batch-size 2)) => [ {:id 1} {:id 2} {:id 3} {:id 4} ]
    (provided
      (execute anything) =streams=> [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] []] ))

  (fact
    (doall (qseq (q/table :foo) :batch-size 2)) => [ ]
    (provided
      (execute anything) =streams=> [ [] ] ))
  )
