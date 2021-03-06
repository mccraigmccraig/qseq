(ns qseq.core-test
  (:use clojure.test
        midje.sweet
        qseq.impl
        qseq.dispatch
        qseq.core)
  (:require [clojure.string :as str]
            [korma.core :as k]))

(fact
  (against-background (before :facts (set-default-transactor ---transactor---) ) (after :facts (set-default-transactor nil)))
  @*default-transactor* => ---transactor---)

(fact
  (against-background (before :facts (do (set-default-transactor ---transactor---) (set-default-transactor nil))))
  @*default-transactor* => nil)

(fact
  (against-background (before :facts (set-default-transactor ---transactor---) ) (after :facts (set-default-transactor nil)))
  (with-default-transactor ---thread-transactor---
    @*default-transactor* => ---thread-transactor---))

(fact
  (transaction (fn [f] (f)) (+ 12 15)) => 27)

(defn cql-qstr
  [q]
  (str/trim (with-out-str (prn q))))

(defn k-qstr
  [q]
  (k/sql-only (k/exec q)))

(fact
  (k-qstr (q-boundary-value (k/select* :foo))) =>
  "SELECT \"foo\".* FROM \"foo\" ORDER BY \"foo\".\"id\" DESC LIMIT 1"

  (k-qstr (q-boundary-value (k/select* :foo) :key :bar)) =>
  "SELECT \"foo\".* FROM \"foo\" ORDER BY \"foo\".\"bar\" DESC LIMIT 1"

  (k-qstr (q-boundary-value (k/select* :foo) :key [:bar :baz])) =>
  "SELECT \"foo\".* FROM \"foo\" ORDER BY \"foo\".\"bar\" DESC, \"foo\".\"baz\" DESC LIMIT 1"

  (k-qstr (q-boundary-value (k/select* :foo) :key [:bar :baz] :operator '>)) =>
  "SELECT \"foo\".* FROM \"foo\" ORDER BY \"foo\".\"bar\" ASC, \"foo\".\"baz\" ASC LIMIT 1"

  (k-qstr (q-boundary-value (k/select* :foo) :boundary 10)) =>
  "SELECT \"foo\".* FROM \"foo\" WHERE ((\"foo\".\"id\" <= ?)) ORDER BY \"foo\".\"id\" DESC LIMIT 1"

  (k-qstr (q-boundary-value (k/select* :foo) :key [:bar :baz] :operator '> :boundary [10 20])) =>
  "SELECT \"foo\".* FROM \"foo\" WHERE ((\"foo\".\"bar\" > ?) OR (\"foo\".\"bar\" = ? AND \"foo\".\"baz\" > ?)) ORDER BY \"foo\".\"bar\" ASC, \"foo\".\"baz\" ASC LIMIT 1")

(fact
  (k-qstr (q-bounded (k/select* :foo) :boundary 10)) =>
  "SELECT \"foo\".* FROM \"foo\" WHERE ((\"foo\".\"id\" <= ?))"

  (k-qstr (q-bounded (k/select* :foo) :key [:bar :baz] :boundary [10 20])) =>
  "SELECT \"foo\".* FROM \"foo\" WHERE ((\"foo\".\"bar\" < ?) OR (\"foo\".\"bar\" = ? AND \"foo\".\"baz\" <= ?))")


(with-default-transactor (fn [f] (f))

  (fact
    (doall (qseq-batches (k/select* :foo) :batch-size 2)) => [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] [{:id 5}]]
    (provided
      (qseq.korma/execute anything) =streams=> [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] [{:id 5}]] ))

  (fact
    (doall (qseq-batches (k/select* :foo) :batch-size 2)) => [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] []]
    (provided
      (qseq.korma/execute anything) =streams=> [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] []] ))

  (fact
    (doall (qseq-batches (k/select* :foo) :batch-size 2)) => [ [] ]
    (provided
      (qseq.korma/execute anything) =streams=> [ [] ] ))

  (fact
    (doall (qseq-batches (k/select* :foo) :batch-size 2)) => [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] []]
    (provided
      (qseq.korma/execute anything) =streams=> [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] []] )))

(with-default-transactor (fn [f] (f))

  (fact
    (doall (qseq (k/select* :foo) :batch-size 2)) => [ {:id 1} {:id 2} {:id 3} {:id 4} {:id 5} ]
    (provided
      (qseq.korma/execute anything) =streams=> [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] [{:id 5}]] ))

  (fact
    (doall (qseq (k/select* :foo) :batch-size 2)) => [ {:id 1} {:id 2} {:id 3} {:id 4} ]
    (provided
      (qseq.korma/execute anything) =streams=> [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] []] ))

  (fact
    (doall (qseq (k/select* :foo) :batch-size 2)) => [ ]
    (provided
      (qseq.korma/execute anything) =streams=> [ [] ] ))

  (fact
    (doall (qseq (k/select* :foo) :batch-size 2)) => [ {:id 1} {:id 2} {:id 3} {:id 4} ]
    (provided
      (qseq.korma/execute anything) =streams=> [ [{:id 1} {:id 2}] [{:id 3} {:id 4}] []] ))  )
