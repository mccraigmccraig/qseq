(ns cql-seq.impl-test
  (:use clojure.test
        cql-seq.impl
        midje.sweet)
  (:require [clojure.string :as str]
            [clojureql.core :as q]))

(defn qstr
  [q]
  (str/trim (with-out-str (prn q))))

(fact
  (boundary-query-sort-direction '<=) => :desc
  (boundary-query-sort-direction "<") => :desc
  (boundary-query-sort-direction :>) => :asc
  (boundary-query-sort-direction '>=) => :asc)

(fact
  (inclusion-operator-for-traversal-direction :asc) => '<=
  (inclusion-operator-for-traversal-direction "asc") => '<=
  (inclusion-operator-for-traversal-direction 'desc) => '>=)

(fact
  (very-lazy-apply-concat [1 2] [[3 4] [5 6]]) => [1 2 3 4 5 6]
  (very-lazy-apply-concat nil [[1 2 3] [4 5 6]]) => [1 2 3 4 5 6]
  (very-lazy-apply-concat [] [[1 2 3] [4 5 6]]) => [1 2 3 4 5 6]
  (very-lazy-apply-concat [1 2] [[] [3 4] [] [5 6] []]) => [1 2 3 4 5 6]
  (very-lazy-apply-concat nil [[] [1 2 3] [] [4 5 6] []]) => [1 2 3 4 5 6])

(fact
  (sort-key (with-meta {} {:key :foo})) => :foo
  (sort-key (with-meta {} {})) => :id)

(fact
  (simple-key-sort "foo") => :foo#asc
  (simple-key-sort :foo :dir :desc) => :foo#desc
  (simple-key-sort :foo :dir "asc") => :foo#asc)

(fact
  (key-sort "foo") => [:foo#asc]
  (key-sort "foo" :dir :asc) => [:foo#asc]
  (key-sort "foo" :dir :desc) => [:foo#desc]
  (key-sort [:foo "bar"]) => [:foo#asc :bar#asc]
  (key-sort [:foo :bar] :dir :asc) => [:foo#asc :bar#asc]
  (key-sort [:foo :bar] :dir :desc) => [:foo#desc :bar#desc])

(fact
  (compound-key-conditions :<= nil [:foo] [10]) => '((and (<= :foo 10)))
  (compound-key-conditions :<= nil [:foo :bar] [10 20]) => '((and (<= :foo 10))
                                                            (and (= :foo 10) (<= :bar 20))))

(fact
  (key-condition :<= :foo 10) => '(or (and (<= :foo 10)))
  (key-condition :<= [:foo :bar] [10 20]) => '(or (and (<= :foo 10))
                                                  (and (= :foo 10) (<= :bar 20)))
  (key-condition :<= [:foo :bar :baz] [10 20 30]) => '(or (and (<= :foo 10))
                                                          (and (= :foo 10) (<= :bar 20))
                                                          (and (= :bar 20) (= :foo 10) (<= :baz 30))))

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
