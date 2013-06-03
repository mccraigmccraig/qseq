(def shared
  '[
    [org.clojure/core.incubator "0.1.2"]
    [org.clojure/tools.logging "0.2.6"]
    [org.clojure/java.jdbc "0.2.3"]
    ])



(defproject qseq "0.4.0"
  :description "qseq: a lazy sequence for simply and efficiently consuming Korma and ClojureQL queries"
  :url "http://github.com/mccraigmccraig/qseq"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"

  :plugins [[lein-midje "3.0-RC1"]
            [codox "0.6.4"]]

  :dependencies ~(conj shared '[org.clojure/clojure "1.5.0"])
  :dev-dependencies []

  :aliases {"all" ["with-profile" "dev:1.3,dev:1.4,dev:1.5"]}
  :profiles {:dev
             {:dependencies [[midje "1.5-RC1" :exclusions [org.clojure/clojure]]
                             [clojureql "1.0.4"]
                             [korma "0.3.0-RC2"]]}
             :1.3
             {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4
             {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5
             {:dependencies [[org.clojure/clojure "1.5.0"]]}}
  )
