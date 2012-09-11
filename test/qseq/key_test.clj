(ns qseq.key_test
  (:use clojure.test
        midje.sweet
        qseq.key))

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
