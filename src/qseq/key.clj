(ns qseq.key
  (:use qseq.util))


(def boundary-query-sort-directions
  "sort directions for querying boundary values under given operators"
  {"<"  :desc
   "<=" :desc
   ">"  :asc
   ">=" :asc})

(defn boundary-query-sort-direction
  "return the sort direction to use such that the first row of a bounded query will
   return the boundary value of the key under the operator"
  [operator]
  (boundary-query-sort-directions (name operator)))

(def inclusion-operators-for-traversal-directions
  "inclusion operators for different sort directions"
  {"asc" '<=
   "desc" '>=})

(defn inclusion-operator-for-traversal-direction
  "given a sort direction return the inclusion operator used to exclude previously seen results with q-outside-boundary"
  [dir]
  (inclusion-operators-for-traversal-directions (name dir)))


(defn compound-key-conditions
  "expand conditions restricting (operator key boundary). eq-conds accumulates equal conditions
   for compound keys"
  [operator eq-conds [key & next-keys] [boundary & next-boundaries]]
  (cons
   `(~'and ~@eq-conds (~(-> operator name symbol) ~key ~boundary))
   (if (not-empty next-keys)
     (compound-key-conditions operator (conj eq-conds `(~'= ~key ~boundary)) next-keys next-boundaries))))

(defn key-condition
  "given an operator and a simple or compound key and corresponding boundary, return query conditions for records
   which meet (key operator boundary)"
  [operator key boundary]
  (let [keys (make-sequential key)
        boundaries (make-sequential boundary)]
    (if (not= (count keys) (count boundaries))
      (throw (RuntimeException. "key and upper-bound must have the same number of components")))
    (let [kc (compound-key-conditions operator nil keys boundaries)]
      `(~'or ~@kc))))
