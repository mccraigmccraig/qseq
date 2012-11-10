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
  (compound-key-conditions :<= nil [:foo] [10]) => '((and (<= :foo 10)))
  (compound-key-conditions :<= nil [:foo :bar] [10 20]) => '((and (< :foo 10))
                                                             (and (= :foo 10) (<= :bar 20))))

(fact
  (key-condition :<= :foo 10) => '(or (and (<= :foo 10)))
  (key-condition :<= [:foo :bar] [10 20]) => '(or (and (< :foo 10))
                                                  (and (= :foo 10) (<= :bar 20)))
  (key-condition :<= [:foo :bar :baz] [10 20 30]) => '(or (and (< :foo 10))
                                                          (and (= :foo 10) (< :bar 20))
                                                          (and (= :bar 20) (= :foo 10) (<= :baz 30))))
