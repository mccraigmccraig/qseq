(ns cql-seq.core-test
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

;.;. The highest reward for a man's toil is not what he gets for it but what
;.;. he becomes by it. -- Ruskin
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
