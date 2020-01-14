(ns diesel.edit-test
  (:use midje.sweet
        diesel.edit))

(facts (as-vec -x-) => [-x-]
       (as-vec '(-x- -y-)) => [-x- -y-]
       (as-vec [-x- -y-]) => [-x- -y-])

(fact (map-it [:a :b :c] #(vector %1 %2) [1 2 3]) => [[:a 1] [:b 2] [:c 3]])

(let [v1 [:a :b :c], v2 [1 2 3], f #(vector %1 %2)]
  (facts (mapv-it v1 f v2) => [[:a 1] [:b 2] [:c 3]]
         (mapv-it v1 f v2) => vector?))

(fact (filter-it [:a :b nil :c] nil?) => [nil])

(fact (remove-it [:a :b nil :c] nil?) => [:a :b :c])

(fact (reduce-it [1 2 3] +) => 6)

(fact (reduce-it [1 2 3] + 10) => 16)

(fact (replace-it [:a :B :c] {:B :b}) => [:a :b :c])

(fact (replace-nth [:a :B :c] {1 :b}) => [:a :b :c])

(fact (rm-nth [:a :b :c :d] 1 3) => [:a :c])

(fact (map-in {-k- [:a :b :c]} [-k-] #(vector %1 %2) [1 2 3]) =>
      {-k- [[:a 1] [:b 2] [:c 3]]})

(let [v1 [:a :b :c], v2 [1 2 3], f #(vector %1 %2)]
  (facts (mapv-in {-k- v1} [-k-] f v2) => {-k- [[:a 1] [:b 2] [:c 3]]}
         (mapv-in {-k- v1} [-k-] f v2) => #(vector? (get-in % [-k-]))))

(fact (filter-in {-k- [:a :b nil :c]} [-k-] nil?) => {-k- [nil]})

(fact (remove-in {-k- [:a :b nil :c]} [-k-] nil?) => {-k- [:a :b :c]})

(fact (rm-in {-k- [:a :b :c]} [-k-] :a :c) => {-k- [:b]})

(fact (rm-nth-in {-k- [:a :b :c]} [-k-] 0 2) => {-k- [:b]})

(fact (reduce-in {-k- [1 2 3]} [-k-] +) => {-k- 6})

(fact (reduce-in {-k- [1 2 3]} [-k-] + 10) => {-k- 16})

(fact (replace-in {-k- [:a :B :c]} [-k-] {:B :b}) => {-k- [:a :b :c]})

(fact (replace-nth-in {-k- [:a :B :c]} [-k-] {1 :b}) => {-k- [:a :b :c]})

(fact (dissoc-in {-k- {:a 1 :b 2 :c 3}} [-k-] :b :c) => {-k- {:a 1}})

(fact (merge-in {-k- {:a 1 :b 2 :c 4}} [-k-] {:c 3 :d 4}) =>
      {-k- {:a 1 :b 2 :c 3 :d 4}})

(fact (edit-in {-k- {:a 1 :b 2 :c 4}} [-k-] :c 3  {:d 4}) =>
      {-k- {:a 1 :b 2 :c 3 :d 4}})

(fact (cons-in {-k- [1 2 3]} [-k-] 0) => {-k- [0 1 2 3]})

(fact (consv-in {-k- [1 2 3]} [-k-] 0) => #(and (vector? (get % -k-))
                                                (= % {-k- [0 1 2 3]})))

(fact (conj-in {-k- [1 2 3]} [-k-] 4) => {-k- [1 2 3 4]})

(fact (conjv-in {-k- '(1 2 3)} [-k-] 4) => {-k- [1 2 3 4]})

(fact (disj-in {-k- #{1 2 3}} [-k-] 1 3) => {-k- #{2}})

(fact (map-vals-in {-k- {:a 1 :b 2 :c 3}} [-k-] dec) =>
      {-k- {:a 0 :b 1 :c 2}})
