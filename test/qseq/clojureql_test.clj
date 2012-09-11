(ns qseq.clojureql_test
  (:use clojure.test
        midje.sweet
        qseq.clojureql)
  (:require [clojure.string :as str]
            [clojureql.core :as q]))

(fact
  (sort-key (with-meta {} {:key :foo})) => :foo
  (sort-key (with-meta {} {})) => :id)

(defn qstr
  [q]
  (str/trim (with-out-str (prn q))))

(fact
  (qstr (q-inside-boundary (q/table :foo) '< :id 10)) =>
  "SELECT foo.* FROM foo WHERE (((foo.id < 10)))"

  (qstr (q-inside-boundary (q/table :foo) '< [:name :age] ["smith" 50])) =>
  "SELECT foo.* FROM foo WHERE (((foo.name < smith)) OR ((foo.name = smith) AND (foo.age < 50)))")

(fact
  (qstr (q-outside-boundary (q/table :foo) '< :id 10)) =>
  "SELECT foo.* FROM foo WHERE NOT((((foo.id < 10))))"

  (qstr (q-outside-boundary (q/table :foo) '< [:name :age] ["smith" 50])) =>
  "SELECT foo.* FROM foo WHERE NOT((((foo.name < smith)) OR ((foo.name = smith) AND (foo.age < 50))))")
