(ns cql-seq.impl-test
  (:use clojure.test
        cql-seq.impl
        midje.sweet))

(fact
  (sql-bound-fn :<=) => :max
  (sql-bound-fn :>=) => :min
  (sql-bound-fn :<) => (throws RuntimeException))

(fact
  (sort-key (with-meta {} {:key :foo})) => :foo
  (sort-key (with-meta {} {})) => :id)


(fact
  (simple-key-transform-col :max :foo) => ["max(foo)" :as :max_foo])

(fact
  (key-transform-cols :max :foo) => ["max(foo)" :as :max_foo]
  (key-transform-cols :max [:foo :bar])=> [["max(foo)" :as :max_foo]
                                           ["max(bar)" :as :max_bar]])

(fact
  (very-lazy-concat [1 2] [[3 4] [5 6]]) => [1 2 3 4 5 6]
  (very-lazy-concat nil [[1 2 3] [4 5 6]]) => [1 2 3 4 5 6]
  (very-lazy-concat [] [[1 2 3] [4 5 6]]) => [1 2 3 4 5 6]
  (very-lazy-concat [1 2] [[] [3 4] [] [5 6] []]) => [1 2 3 4 5 6]
  (very-lazy-concat nil [[] [1 2 3] [] [4 5 6] []]) => [1 2 3 4 5 6])

(fact
  (compound-key-conditions :<= nil [:foo] [10]) => '((and (<= :foo 10)))
  (compound-key-conditions :<= nil [:foo] [10]) => '((and (<= :foo 10)))
  (compound-key-conditions :<= nil [:foo :bar] [10 20]) => '((and (<= :foo 10))
                                                            (and (= :foo 10) (<= :bar 20))))

;.;. The biggest reward for a thing well done is to have done it. -- Voltaire
(fact
  (key-condition :<= :foo 10) => '(or (and (<= :foo 10)))
  (key-condition :<= [:foo :bar] [10 20]) => '(or (and (<= :foo 10))
                                                  (and (= :foo 10) (<= :bar 20)))
  (key-condition :<= [:foo :bar :baz] [10 20 30]) => '(or (and (<= :foo 10))
                                                          (and (= :foo 10) (<= :bar 20))
                                                          (and (= :bar 20) (= :foo 10) (<= :baz 30)))
  )
