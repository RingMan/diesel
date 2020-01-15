(ns diesel.core-test
  (:use midje.sweet
        diesel.core))

(defn property? [m]
  (and (map? m) (= 1 (count m))))

(defn property-is [pred? p]
  (-> p first val pred?))

(fact (mk-prop -k- -v-) => {-k- -v-})

(facts "mk-bool-prop returns a property whose value is boolean"
       (fact "value defaults to true"
             (mk-bool-prop -k-) => {-k- true})

       (fact "truthy values coerce to true"
             (mk-bool-prop -k- :truthy) => {-k- true})

       (fact "falsey values coerce to false"
             (mk-bool-prop -k- nil) => {-k- false}))

(fact
  "mk-map-prop creates a property whose value is a map"
  (mk-map-prop -k-) => {-k- {}}
  (let [args [-k- :k1 :v1 :k2 :v2]]
    (apply mk-map-prop args) => property?
    (apply mk-map-prop args) => (partial property-is map?)
    (apply mk-map-prop args) => {-k- {:k1 :v1 :k2 :v2}}))

(fact
  "mk-vec-prop creates a property whose value is a vector"
  (mk-vec-prop -k-) => {-k- []}
  (let [args [-k- -a- -b- -c-]]
    (apply mk-vec-prop args) => property?
    (apply mk-vec-prop args) => (partial property-is vector?)
    (apply mk-vec-prop args) => {-k- [-a- -b- -c-]}))

(fact (mk-unit-prop -k- -v- -u-) => {-k- {:units -u- :val -v-}})

(facts
  "about mk-pct-prop"
  (let [pct 34 pct-str "34.0 percent"]
    (mk-pct-prop -k- pct :bad-units) => (throws java.lang.AssertionError
                                                "Assert failed: (pct-units u)")
    (mk-pct-prop -k- (/ pct 100) :dec) => {-k- pct-str}
    (mk-pct-prop -k- pct :percent) => {-k- pct-str}
    (mk-pct-prop -k- pct :pct) => {-k- pct-str}))

(fact (mk-loc-prop -k- -lat- -lon-) => {-k- {:lat -lat- :lon -lon-}})

(fact
  (mk-prop-makers mk-prop age weight) =expands-to=>
  (do
    (def age (clojure.core/partial mk-prop :age))
    (def weight (clojure.core/partial mk-prop :weight))))

(fact
  (def-props age weight) =expands-to=>
  (diesel.core/mk-prop-makers diesel.core/mk-prop age weight))

(fact
  (def-pct-props probability likelihood) =expands-to=>
  (diesel.core/mk-prop-makers diesel.core/mk-pct-prop probability likelihood))

(fact
  (def-unit-props length duration) =expands-to=>
  (diesel.core/mk-prop-makers diesel.core/mk-unit-prop length duration))

(fact
  (def-loc-props position gps-loc) =expands-to=>
  (diesel.core/mk-prop-makers diesel.core/mk-loc-prop position gps-loc))

