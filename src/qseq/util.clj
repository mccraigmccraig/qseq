(ns qseq.util
  )

(defn very-lazy-apply-concat
  "lazier than (apply concat seqs)... evaluates nothing at construction time"
  [coll colls]
  (lazy-seq
    (cond
      (and (empty? coll) (empty? colls)) nil
      (empty? coll) (very-lazy-apply-concat (first colls) (next colls))
      true (cons (first coll) (very-lazy-apply-concat (next coll) colls)))))
