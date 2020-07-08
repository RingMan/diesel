(ns diesel.core-test
  (:use midje.sweet
        diesel.core))

(defn property? [m]
  (and (map? m) (= 1 (count m))))

(defn property-is [pred? p]
  (-> p first val pred?))

(fact "Use mk-map to create maps from scratch using the key helper function, mk-map*"
  (fact "With no args, mk-map just returns an empty map"
    (mk-map) => {})
  (fact "If an arg in 'keyword' position is nil, skip it. Together with the
        splicing rule below, this let's you inline conditional logic."
    (mk-map nil :k :v nil {:k2 :v2} nil) => {:k :v, :k2 :v2}
    (mk-map :k :v (when false [:k0 :v0]) :k2 :v2 (if true nil [:k3 :v3]))
    => {:k :v, :k2 :v2})
  (fact "mk-map acts like `merge` when an arg in 'keyword' position is a map"
    (mk-map :k :v {:k2 :V2} {:k2 :v2, :k3 :v3}) => {:k :v, :k2 :v2, :k3 :v3})
  (fact "When an arg in 'keyword' position is a function, mk-map* calls it
        with the current value of the map."
    (mk-map {:k :v} #(assoc % :k2 1) #(update % :k2 inc)) => {:k :v, :k2 2})
  (fact "When an arg in 'keyword' position is sequential, mk-map* splices
        the elements into the argument list."
    (mk-map {:k :v} [:k2 :v2] (list :k3 :v3))
    => {:k :v, :k2 :v2, :k3 :v3}
    (mk-map {:k :v} [[[nil :k2 :v2 [[{:k3 :v3} nil]]]]])
    => {:k :v, :k2 :v2, :k3 :v3})
  (fact "mk-map acts like `assoc` when given key/value pairs where the value
        in 'keyword' position isn't 'special'."
    (mk-map :k :v 5 "five" "map" {:k3 :v3} 'sym :sym \tab :tab)
    => {:k :v, 5 "five", "map" {:k3 :v3}, 'sym :sym, \tab :tab}
    (mk-map :k :v :key-with-no-val)
    => (throws "mk-map* missing value after key: :key-with-no-val"))
  (fact "`nil`, maps, lists, vectors, and functions can still act as keys
        if you wrap them in a map"
    (mk-map {nil :nil} {{:k :v} :map} {'(:key) :list} {[:vec] :vec} {inc :inc})
    => {nil :nil, {:k :v} :map, '(:key) :list, [:vec] :vec, inc :inc}))

(fact "Use `edit` to edit existing maps"
  (edit {:k1 0} :k2 2 #(update % :k1 inc) nil {:k3 3} [:k4 4])
  => {:k1 1, :k2 2, :k3 3, :k4 4})

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
  (def-bool-props nice? funny?) =expands-to=>
  (diesel.core/mk-prop-makers diesel.core/mk-bool-prop nice? funny?))

(fact
  (def-map-props p1 p2) =expands-to=>
  (diesel.core/mk-prop-makers diesel.core/mk-map-prop p1 p2))

(fact
  (def-vec-props p1 p2) =expands-to=>
  (diesel.core/mk-prop-makers diesel.core/mk-vec-prop p1 p2))

(fact
  (def-pct-props probability likelihood) =expands-to=>
  (diesel.core/mk-prop-makers diesel.core/mk-pct-prop probability likelihood))

(fact
  (def-unit-props length duration) =expands-to=>
  (diesel.core/mk-prop-makers diesel.core/mk-unit-prop length duration))

(fact
  (def-loc-props position gps-loc) =expands-to=>
  (diesel.core/mk-prop-makers diesel.core/mk-loc-prop position gps-loc))

;define two properties of books
(def-props author pages)
;generate functions to create book entities
(def-entity-maps book)
(def-entity-maps {:tag 1} bookWithTag)
(def-entity-maps {:id :title} bookWithId)
(def-entity-maps {:id :entity, :tag true} bookWithIdAndTag)

(fact (author -a-) => {:author -a-})
(fact (pages -p-) => {:pages -p-})

(fact "book adds nothing extra to Entity properties"
      (book (pages -p-) (author -a-)) => (just {:author -a- :pages -p-}))

(fact "bookWithTag adds :tag to Entity properties"
      (bookWithTag (pages -p-) (author -a-)) =>
      (just {:tag :bookWithTag :author -a- :pages -p-}))

(fact "bookWithId takes id as first arg and adds :title to Entity properties"
      (bookWithId -id- (pages -p-) (author -a-)) =>
      (just {:title -id- :author -a- :pages -p-}))

(fact "bookWithIdAndTag takes id as first arg and adds :entity and :tag
      to Entity properties"
      (bookWithIdAndTag -id- (pages -p-) (author -a-)) =>
      (just {:tag :bookWithIdAndTag :entity -id- :author -a- :pages -p-}))

;define two properties of cars
(def-props model vin)
;generate functions to create properties whose value is a car Entity
(def-entity-props car)
(def-entity-props {:tag 1} carWithTag)
(def-entity-props {:id :nickname} carWithId)
(def-entity-props {:id :handle, :tag true} carWithIdAndTag)

(fact (model -m-) => {:model -m-})
(fact (vin -v-) => {:vin -v-})

(fact "car takes id as first arg but adds nothing extra to Entity properties"
      (car -id- (vin -v-) (model -m-)) => (just {-id- {:model -m- :vin -v-}}))

(fact "carWithTag takes id as first arg and adds :tag to Entity properties"
      (carWithTag -id- (vin -v-) (model -m-)) =>
      (just {-id- {:tag :carWithTag :model -m- :vin -v-}}))

(fact "carWithId takes id as first arg and adds :nickname to Entity properties"
      (carWithId -id- (vin -v-) (model -m-)) =>
      (just {-id- {:nickname -id- :model -m- :vin -v-}}))

(fact "carWithIdAndTag takes id as first arg and adds :handle and :tag
      to Entity properties"
      (carWithIdAndTag -id- (vin -v-) (model -m-)) =>
      (just {-id- {:tag :carWithIdAndTag :handle -id- :model -m- :vin -v-}}))

;macro is only suitable for functions that take id as first arg, but fn
;can return either an Entity or an Entity Property

(def-entity-macros bookWithId bookWithIdAndTag)

(defbookWithId myBkId (author :me) (pages 100))
(fact myBkId => (just {:title :myBkId :author :me :pages 100}))

(defbookWithIdAndTag myBkIdTag (author :me) (pages 100))
(fact myBkIdTag => (just {:tag :bookWithIdAndTag :entity :myBkIdTag :author :me :pages 100}))

;how you might generate macros for a DSL describing car entities
;macro is suitable to use with any function created by def-entity-props

(def-entity-macros car carWithTag carWithId carWithIdAndTag)

;how client code might invoke your generated macros

(defcar myCar (vin 123) (model :T))
(defcarWithTag myCarTag (vin 123) (model :T))
(defcarWithId myCarId (vin 123) (model :T))
(defcarWithIdAndTag myCarIdTag (vin 123) (model :T))

(fact myCar => (just {:myCar {:vin 123 :model :T}}))
(fact myCarTag => (just {:myCarTag {:tag :carWithTag :vin 123 :model :T}}))
(fact myCarId => (just {:myCarId {:nickname :myCarId :vin 123 :model :T}}))
(fact myCarIdTag => (just {:myCarIdTag {:handle :myCarIdTag :tag :carWithIdAndTag :vin 123 :model :T}}))
