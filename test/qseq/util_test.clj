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
