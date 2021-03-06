(ns qseq.util
  (:use clojure.core.incubator))

(defn very-lazy-apply-concat
  "lazier than (apply concat seqs)... evaluates nothing at construction time"
  [coll colls]
  (lazy-seq
    (cond
      (and (empty? coll) (empty? colls)) nil
      (empty? coll) (very-lazy-apply-concat (first colls) (next colls))
      true (cons (first coll) (very-lazy-apply-concat (next coll) colls)))))

(defn make-sequential
  "if v is already sequential return it unchanged. if not, return [v]"
  [v]
  (if (sequential? v) v [v]))

(defn pick
  "pick keys from the first element of a resultset"
  [results keyseq]
  (-?> results
       first
       (select-keys keyseq)))
