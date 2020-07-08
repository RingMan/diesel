(ns user
  (:require [clojure.core :as c]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer (pprint)]
            [clojure.repl :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [expound.alpha :refer [expound]]
            [integrant.repl :refer [clear go halt prep init reset reset-all]]
            [me.raynes.fs :as fs]
            [midje.repl :refer [autotest]]
            [diesel.core :refer :all :as dsl]
            [diesel.edit :refer :all :as ed]))

(integrant.repl/set-prep! (constantly {}))

