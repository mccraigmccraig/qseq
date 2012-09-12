(ns qseq.util_test
  (:use clojure.test
        midje.sweet
        qseq.util))

(fact
  (very-lazy-apply-concat [1 2] [[3 4] [5 6]]) => [1 2 3 4 5 6]
  (very-lazy-apply-concat nil [[1 2 3] [4 5 6]]) => [1 2 3 4 5 6]
  (very-lazy-apply-concat [] [[1 2 3] [4 5 6]]) => [1 2 3 4 5 6]
  (very-lazy-apply-concat [1 2] [[] [3 4] [] [5 6] []]) => [1 2 3 4 5 6]
  (very-lazy-apply-concat nil [[] [1 2 3] [] [4 5 6] []]) => [1 2 3 4 5 6])

(fact
  (make-sequential 1) => [1]
  (make-sequential "foo") => ["foo"]
  (make-sequential [1 2 3]) => [1 2 3])

(fact
  (pick [{:foo 1 :bar 2 :baz 3} {:foo 5 :bar 6 :baz 7}] [:foo :bar]) => {:foo 1 :bar 2}
  (pick nil [:foo :bar]) => nil)
