(defproject qseq "0.2.2"
  :description "qseq: a lazy sequence for simply and efficiently consuming Korma and ClojureQL queries"
  :url "http://github.com/mccraigmccraig/qseq"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojars.mccraigmccraig/core.incubator "0.1.1-20111019.122151-1"]
                 [org.clojure/tools.logging "0.2.4"]
                 [org.clojure/java.jdbc "0.2.2"]]
  :profiles {:dev
             {:dependencies [[midje "1.4.0" :exclusions [org.clojure/clojure]]
                             [clojureql "1.0.4"]
                             [mccraigmccraig/korma "0.3.0-beta11"]]}
             :1.3
             {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4
             {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5
             {:dependencies [[org.clojure/clojure "1.5.0-alpha4"]]}}
  :min-lein-version "2.0.0"
  :plugins [[lein-midje "2.0.0-SNAPSHOT"]
            [codox "0.6.1"]])
