(ns qseq.dispatch
  "dispatch methods to Korma or ClojureQL implementations"
  (:require qseq.korma
            qseq.clojureql)
  (:import [clojure.lang  APersistentMap]))

(def dispatch-methods [:sort-key
                       :q-empty
                       :q-inside-boundary
                       :q-outside-boundary
                       :q-sorted
                       :q-limited
                       :execute])

(defn dispatch-fn
 [& args]
 (if (isa? (type (first args)) APersistentMap)
   :korma
   :clojureql))

(dorun (map (fn [method]
              (let [mname (name method)
                    msym (symbol mname)]
                (eval `(defmulti ~msym dispatch-fn))
                (eval `(defmethod ~msym :korma [& args#] (apply #'~(symbol "qseq.korma" mname) args#)))
                (eval `(defmethod ~msym :clojureql [& args#] (apply #'~(symbol "qseq.clojureql" mname) args#)))))
            dispatch-methods))
