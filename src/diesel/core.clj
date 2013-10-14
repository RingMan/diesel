(ns diesel.core
  (:require [clojure.string :as str]))

(def pct-units #{:pct :percent :dec})

(defn mk-map* [m args]
  (let [[x & xs] args]
    (cond
      (nil? args) m
      (nil? x) (recur m xs)
      (map? x) (recur (merge m x) xs)
      (coll? x) (recur m (concat x xs))
      (fn? x) (recur (x m) xs)
      (keyword? x) (recur (assoc m x (first xs)) (rest xs))
      :else (throw (RuntimeException.
                     (str "Illegal arg to mk-map*: " x))))))

(defn mk-prop [k v]
  {k v})

(defn mk-bool-prop
  ([k] {k true})
  ([k v] {k (boolean v)}))

(defn mk-map-prop [k & body]
  {k (mk-map* {} body)})

(defn mk-vec-prop [k & xs] {k (vec xs)})

(defn mk-unit-prop [k v u]
  {k {:val v :units u}})

(defn mk-pct-prop [k v u]
  (assert (pct-units u))
  {k (str (double (if (= :dec u) (* 100 v) v)) " percent")})

(defn mk-loc-prop [k lat lon]
  {k {:lat lat :lon lon}})

(defmacro mk-prop-makers [mk-prop-func & syms]
  (cons 'do
        (for [sym syms]
          `(def ~sym (partial ~mk-prop-func ~(keyword sym))))))

(defmacro def-props [& syms]
  `(mk-prop-makers mk-prop ~@syms))

(defmacro def-bool-props [& syms]
  `(mk-prop-makers mk-bool-prop ~@syms))

(defmacro def-map-props [& syms]
  `(mk-prop-makers mk-map-prop ~@syms))

(defmacro def-vec-props [& syms]
  `(mk-prop-makers mk-vec-prop ~@syms))

(defmacro def-pct-props [& syms]
  `(mk-prop-makers mk-pct-prop ~@syms))

(defmacro def-unit-props [& syms]
  `(mk-prop-makers mk-unit-prop ~@syms))

(defmacro def-loc-props [& syms]
  `(mk-prop-makers mk-loc-prop ~@syms))

(defn property [id & body]
  {id (mk-map* {} body)})

(defn mk-map-form [{:keys [id tag]} sym]
  `(mk-map* ~(merge {}
                    (when tag {:tag (keyword sym)})
                    (when id {id 'id})) ~'body))

(defmacro def-dyn-props
  "Creates functions or function aliases for defining 'dynamic' properties
  (i.e., those where the property id is passed as an argument).  The macro
  accepts one or more symbols possibly preceded by an options map.  If
  the :tag option is truthy, generated maps will have a :tag key whose
  value matches the function name.  If :id is supplied, its value will be
  used as an ID key in generated maps.

  Example:
    (def-dyn-props prop1 prop2)
    (def-dyn-props {:tag true} prop3 prop4)
    (def-dyn-props {:id :name} prop5 prop6)
    (def-dyn-props {:id :id, :tag :truthy} prop7)

    (prop1 :age 4 :dob 2001) => {:prop1 {:age 4 :dob 2001}}
    (prop3 :age 4 :dob 2001) => {:prop1 {:age 4 :dob 2001 :tag :prop3}}
    (prop5 :fred :age 4 :dob 2001) => {:prop1 {:age 4 :dob 2001 :name :fred}}
    (prop7 :fred :age 4 :dob 2001) =>
      {:prop1 {:age 4 :dob 2001 :id :fred :tag :prop7}}"
  [opt & syms]
  (let [{:keys [id tag]} opt
        syms (if (map? opt) syms (cons opt syms))]
    (if (or id tag)
      (cons 'do
            (for [sym syms]
              `(defn ~sym [~'id & ~'body]
                 {~'id ~(mk-map-form opt sym)})))
      (cons 'do
            (for [sym syms]
              `(def ~sym property))))))

(defmacro def-entity-maps
  "Creates functions for creating maps representing an entity.  The macro
  accepts one or more symbols possibly preceded by an options map.  See
  def-dyn-props for details

  Example:
    (def-entity-props {:tag 1 :id :id} person)

    (person :fred :age 4 :dob 2001) =>
      {:tag :person :id :fred :age 4 :dob 2001}"
  [opt & syms]
  (let [{:keys [id tag]} opt
        syms (if (map? opt) syms (cons opt syms))]
    (cons 'do
          (for [sym syms]
            `(defn ~sym ~(if id '[id & body] '[& body])
               ~(mk-map-form opt sym))))))

(defmacro def-entity-macros
  "Creates an entity-generating macro for each symbol in syms.
  For example (def-entity-macros scenario) creates a macro like

  (defmacro defwidget [id & body]
    `(def ~id (widget ~(name id) ~@body)))"
  [& syms]
  (cons 'do
        (for [sym syms]
          `(defmacro ~(symbol (str "def" (name sym))) [~'id & ~'body]
             `(def ~~'id ('~~sym ~~'(keyword (name id)) ~@~'body))))))

(defn alter-in
  "Merges args into map
  If an arg is a keyword, the next arg is taken to be its value
  Map args are merged in
  Function args are invoked with the current map passed in"
  [m & args]
  (mk-map* m args))
