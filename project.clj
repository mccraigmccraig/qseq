(def shared
  '[
    [org.clojure/core.incubator "0.1.3"]
    [org.clojure/tools.logging "0.3.1"]
    [org.clojure/java.jdbc "0.3.6"]
    ])



(defproject qseq "0.6.1"
  :description "qseq: a lazy sequence for simply and efficiently consuming Korma"
  :url "http://github.com/mccraigmccraig/qseq"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"

  :plugins [[lein-midje "3.1.3"]
            [codox "0.8.10"]]

  :dependencies ~(conj shared '[org.clojure/clojure "1.6.0"])
  :dev-dependencies []

  :aliases {"all" ["with-profile" "dev:1.4,dev:1.5,dev:1.6"]}
  :profiles {:dev
             {:dependencies [[midje "1.6.3" :exclusions [org.clojure/clojure]]
                             [korma "0.4.0"]]}
             :1.4
             {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5
             {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.6
             {:dependencies [[org.clojure/clojure "1.6.0"]]}}
  )
