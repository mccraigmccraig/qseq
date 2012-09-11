(defproject qseq "0.1.0-SNAPSHOT"
  :description "qseq: a lazy sequence for simply and efficiently consuming Korma and ClojureQL queries"
  :url "http://github.com/mccraigmccraig/qseq"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojars.mccraigmccraig/core.incubator "0.1.1-SNAPSHOT"]
                 [org.clojure/java.jdbc "0.1.4"]
                 [clojureql "1.0.3"]]
  :profiles {:dev
             {:dependencies [[midje "1.4.0" :exclusions [org.clojure/clojure]]]}}
  :min-lein-version "2.0.0"
  :plugins [[lein-midje "2.0.0-SNAPSHOT"]])
