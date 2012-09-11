(ns qseq.despatch
  (:require qseq.korma
            qseq.clojureql)
  (:import [clojure.lang PersistentArrayMap]))

(defmulti q-inside-boundary type)
(defmulti q-outside-boundary type)
(defmulti q-sorted type)
(defmulti q-seq-batch type)

;; korma implementations

(defmethod q-inside-boundary PersistentArrayMap
  [& args]
  (apply qseq.korma/q-inside-boundary args))

(defmethod q-outside-boundary PersistentArrayMap
  [& args]
  (apply qseq.korma/q-outside-boundary args))

(defmethod q-sorted PersistentArrayMap
  [& args]
  (apply qseq.korma/q-sorted args))

(defmethod q-seq-batch PersistentArrayMap
  [& args]
  (apply qseq.korma/q-seq-batch args))

;; clojureql implementations

(defmethod q-inside-boundary :default
  [& args]
  (apply qseq.clojureql/q-inside-boundary args))

(defmethod q-outside-boundary :default
  [& args]
  (apply qseq.clojureql/q-outside-boundary args))

(defmethod q-sorted PersistentArrayMap
  [& args]
  (apply qseq.clojureql/q-sorted args))

(defmethod q-seq-batch PersistentArrayMap
  [& args]
  (apply qseq.clojureql/q-seq-batch args))
