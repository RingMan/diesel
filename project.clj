(defproject diesel "0.2.0-SNAPSHOT"
  :description "diesel = di-es-el = DSL helper library"
  :url "https://github.com/RingMan/diesel"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.3.1"]
                                  [integrant "0.8.0"]
                                  [midje "1.9.9"]]
                   :plugins [[lein-cloverage "1.1.2"]
                             [lein-midje "3.2.2"]]}
             :repl {:dependencies [[clj-commons/fs "1.5.2"]
                                   [expound "0.8.5"]
                                   [integrant/repl "0.3.1"]]
                    :source-paths ["dev"]}})
