(ns cql-seq.core-test
  (:use clojure.test
        cql-seq.core
        midje.sweet))


;.;. [31mFAIL[0m at (NO_SOURCE_FILE:2)
;.;.     Expected: false
;.;.       Actual: true
(fact
  true => true)
