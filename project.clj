(defproject diesel "0.1.2"
  :description "diesel = di-es-el = DSL helper library"
  :url "https://github.com/RingMan/diesel"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git"
        :url "https://github.com/RingMan/diesel"}
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :profiles {:dev {:dependencies [[midje "1.5-beta1"]]
                   :plugins [[lein-midje "3.0-beta1"]]}})
