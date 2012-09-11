(ns qseq.korma_test
  (:use clojure.test
        midje.sweet
        qseq.korma)
  (:require [clojure.string :as str]
            [korma.core :as k]))

(k/defentity foo)
(k/defentity bar (assoc :pk :name))
(k/defentity baz (assoc :pk [:name :age]))

(fact
  (sort-key (k/select* foo)) => :id
  (sort-key (k/select* bar)) => :name
  (sort-key (k/select* baz)) => [:name :age]
  (sort-key (k/select* :foo)) => :id)

(defn qstr
  [q]
  (k/sql-only (k/exec q)))

(fact
  (qstr (q-inside-boundary (k/select* foo) '< :id 10)) =>
  "SELECT \"foo\".* FROM \"foo\" WHERE ((\"foo\".\"id\" < ?))"

  (qstr (q-inside-boundary (k/select* foo) '< [:name :age] ["smith" 50])) =>
  "SELECT \"foo\".* FROM \"foo\" WHERE ((\"foo\".\"name\" < ?) OR (\"foo\".\"name\" = ? AND \"foo\".\"age\" < ?))")

(fact
  (qstr (q-outside-boundary (k/select* foo) '< :id 10)) =>
  "SELECT \"foo\".* FROM \"foo\" WHERE NOT(((\"foo\".\"id\" < ?)))"

  (qstr (q-outside-boundary (k/select* foo) '< [:name :age] ["smith" 50])) =>
  "SELECT \"foo\".* FROM \"foo\" WHERE NOT(((\"foo\".\"name\" < ?) OR (\"foo\".\"name\" = ? AND \"foo\".\"age\" < ?)))")
