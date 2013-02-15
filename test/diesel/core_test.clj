(ns diesel.core-test
  (:use midje.sweet
        diesel.core))

(fact (mk-prop -k- -v-) => {-k- -v-})

(fact (mk-unit-prop -k- -v- -u-) => {-k- {:units -u- :val -v-}})

(facts
  "about mk-pct-prop"
  (let [pct 34 pct-str "34.0 percent"]
    (mk-pct-prop -k- pct :bad-units) => (throws java.lang.AssertionError
                                                "Assert failed: (pct-units u)")
    (mk-pct-prop -k- (/ pct 100) :dec) => {-k- pct-str}
    (mk-pct-prop -k- pct%) => {-k- pct-str}
    (mk-pct-prop -k- pct :percent) => {-k- pct-str}
    (mk-pct-prop -k- pct :pct) => {-k- pct-str}))

(fact (mk-loc-prop -k- -lat- -lon-) => {-k- {:lat -lat- :lon -lon-}})

(fact
  (mk-prop-makers mk-prop age weight) =expands-to=>
  (do
    (def age (clojure.core/partial mk-prop :age))
    (def weight (clojure.core/partial mk-prop :weight))))
