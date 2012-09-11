(ns qseq.despatch
  (:require qseq.korma
            qseq.clojureql)
  (:import [clojure.lang PersistentArrayMap]))

(def despatch-methods [:q-inside-boundary
                       :q-outside-boundary
                       :q-sorted
                       :q-seq-batch])

(dorun (map (fn [method]
              (let [mname (name method)
                    msym (symbol mname)]
                (eval `(defmulti ~msym type))
                (eval `(defmethod ~msym PersistentArrayMap [& args#] (apply ~(symbol "qseq.korma" mname) args#)))
                (eval `(defmethod ~msym :default [& args#] (apply ~(symbol "qseq.clojureql" mname) args#)))))
            despatch-methods))
