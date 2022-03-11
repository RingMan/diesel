(ns diesel.edit
  (:use [diesel.core :only [mk-map*]]))

(defn as-vec [x]
  (if (sequential? x) (vec x) [x]))

(defn map-it [coll f & colls]
  (apply map f coll colls))

(defn mapv-it [coll f & colls]
  (apply mapv f coll colls))

(defn filter-it [coll pred]
  (filter pred coll))

(defn remove-it [coll pred]
  (remove pred coll))

(defn reduce-it
  ([coll f] (reduce f coll))
  ([coll f v] (reduce f v coll)))

(defn replace-it [coll smap]
  (replace smap coll))

(defn replace-nth [coll smap]
 (map-indexed (fn [ix v] (if-let [e (find smap ix)] (val e) v)) coll))

(defn rm-nth [coll & ixs]
  (let [rm? (set ixs)]
    (->> coll
         (map-indexed #(vector %1 %2))
         (reduce (fn [acc [ix v]] (if (rm? ix) acc (conj acc v))) []))))

(defn map-in [m ks f & colls]
  (apply update-in m ks map-it f colls))

(defn mapv-in [m ks f & colls]
  (apply update-in m ks mapv-it f colls))

(defn filter-in [m ks pred]
  (update-in m ks filter-it pred))

(defn remove-in [m ks pred]
  (update-in m ks remove-it pred))

(defn rm-in [m ks & xs]
  (update-in m ks remove-it (set xs)))

(defn rm-nth-in [m ks & ixs]
  (apply update-in m ks rm-nth ixs))

(defn reduce-in
  ([m ks f] (update-in m ks reduce-it f))
  ([m ks f v] (update-in m ks reduce-it f v)))

(defn replace-in [m ks smap]
  (update-in m ks replace-it smap))

(defn replace-nth-in [m ks smap]
  (update-in m ks replace-nth smap))

(defn dissoc-in [m ks & xs]
  (apply update-in m ks dissoc xs))

(defn merge-in [m ks & maps]
  (apply update-in m ks merge maps))

(defn edit-in [m ks & body]
  (update-in m ks mk-map* body))

(defn cons-in [m ks x]
  (update-in m ks (partial cons x)))

(defn consv-in
  "cons x to the collection indexed by ks and coerce result to a vector"
  [m ks x]
  (update-in m ks #(vec (cons x %))))

(defn conj-in [m ks & xs]
  (apply update-in m ks conj xs))

(defn conjv-in
  "coerce collection indexed by ks to a vector and conj xs to it"
  [m ks & xs]
  (update-in m ks #(apply conj (as-vec %) xs)))

(defn disj-in [m ks & xs]
  (apply update-in m ks disj xs))

(defn map-vals-in [m ks f & args]
  (assoc-in
    m ks
    (reduce (fn [m [k v]] (assoc m k (apply f v args)))
            {} (get-in m ks))))

