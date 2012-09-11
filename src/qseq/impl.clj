(ns qseq.impl
  "back-end agnostic implementation"
  (:use qseq.util
        qseq.key
        qseq.dispatch))

(defn q-seq-batch
  "query retrieving a batch of records sorted by key with (not (operator key lower-boundary))"
  [query batch-size key dir lower-boundary]
  (-> query
      (q-sorted :key key :dir dir)
      (q-outside-boundary (inclusion-operator-for-traversal-direction dir) key lower-boundary)
      (q-limited batch-size)))
