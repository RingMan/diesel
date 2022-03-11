(ns diesel.core
  (:require [clojure.string :as str]))

;; Core helper function to edit existing map

(defn mk-map*
  "Process args recursively to edit an existing map, m. Arguments are processed
  as follows:

   - If args is empty return the current map.
   - If arg is nil, skip it. This lets you use forms like if and when to
     conditionally edit a map.
   - If arg is a map, merge it into the current map
   - If arg is a collection, splice it into args and continue. This lets you
     use for comprehensions and such in calls to mk-map*
   - If arg is a function, apply it to the current map.
   - Otherwise, assume arg forms a key/value pair with the next arg. If the
     value part is a function, apply it to the current value and assoc the
     result to the existing key. Else, just assoc the new value to the given
     key. Both ways consume two arguments. It's an error if value is missing.

  See [[diesel.core-test]] for examples."
  [m args]
  (let [[x & xs] args]
    (cond
      (empty? args) m
      (nil? x) (recur m xs)
      (map? x) (recur (merge m x) xs)
      (sequential? x) (recur m (concat x xs))
      (fn? x) (recur (x m) xs)
      (empty? xs) (throw (RuntimeException.
                           (str "mk-map* missing value after key: " x)))
      :else (let [f (first xs)
                  v (if (fn? f) (f (get m x)) f)]
              (recur (assoc m x v) (rest xs))))))

;; Public API for creating/editing maps

(defn mk-map
  "Makes a new map using mk-map*. See mk-map* for
  how the arguments are processed"
  [& args]
  (mk-map* {} args))

(defn edit
  "Edits an existing map, m, using mk-map*. See mk-map* for
  how the arguments are processed"
  [m & args]
  (mk-map* m args))

;; Helper functions that return a map with a single key/value pair, representing a
;; property of an entity.

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

(def pct-units #{:pct :percent :dec})

(defn mk-pct-prop [k v u]
  (assert (pct-units u))
  {k (str (double (if (= :dec u) (* 100 v) v)) " percent")})

(defn mk-loc-prop [k lat lon]
  {k {:lat lat :lon lon}})

;; Public API (aux): Helper macro for the property creation macros below

(defmacro mk-prop-makers [mk-prop-func & syms]
  (cons 'do
        (for [sym syms]
          `(def ~sym (partial ~mk-prop-func ~(keyword sym))))))

;; Public API: macros for generating property creation functions

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

;; Helper functions for the macros below

(defn property [id & body]
  {id (mk-map* {} body)})

(defn mk-map-form [{:keys [id tag]} sym]
  `(mk-map* ~(merge {}
                    (when tag {:tag (keyword sym)})
                    (when id {id 'id})) ~'body))

;; Public API continued

(defmacro def-entity-props
  "Accepts one or more symbols possibly preceded by an options map. For each
  symbol, creates a function that returns an Entity Property. The first
  argument to each generated function is the Entity name. All remaining
  arguments describe the Entity's properties. If the :tag option is truthy,
  generated maps will have a :tag key whose value matches the function name.
  If :id is supplied, its value will be used as an ID key in generated maps.

  Example:
    (def-entity-props ent1 ent2)
    (def-entity-props {:tag true} ent3 ent4)
    (def-entity-props {:id :name} ent5 ent6)
    (def-entity-props {:id :id, :tag :truthy} ent7)

    (ent1 :fred :age 4 :dob 2001) => {:fred {:age 4 :dob 2001}}
    (ent3 :fred :age 4 :dob 2001) => {:fred {:age 4 :dob 2001 :tag :ent3}}
    (ent5 :fred :age 4 :dob 2001) => {:fred {:age 4 :dob 2001 :name :fred}}
    (ent7 :fred :age 4 :dob 2001) =>
      {:fred {:age 4 :dob 2001 :id :fred :tag :ent7}}"
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
  def-entity-props for details

  Example:
    (def-entity-maps {:tag 1 :id :id} person)

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
  "Creates an entity-generating macro for each symbol in syms,
  where symbol refers to a function that takes an entity id as its
  first argument and any number of arguments for the entity's properties.
  This includes any function created via def-entity-props and functions
  created via def-entity-maps with the :id option set.

  For example (def-entity-macros widget) creates a macro like

  (defmacro defwidget [id & body]
    `(def ~id (widget ~(name id) ~@body)))"
  [& syms]
  (cons 'do
        (for [sym syms]
          `(defmacro ~(symbol (str "def" (name sym))) [~'id & ~'body]
             `(def ~~'id ('~~sym ~~'(keyword (name id)) ~@~'body))))))

