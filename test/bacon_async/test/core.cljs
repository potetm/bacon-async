(ns bacon-async.test.core
  (:require-macros [bacon-async.test.macro :refer (defasync expect-stream-events expect-property-events later) :as m]
                   [cemerick.cljs.test :refer (is deftest with-test run-tests testing test-var done)])
  (:require [bacon-async.core :as b]
            [cemerick.cljs.test]))

(defasync later
  (testing "it should send a single event and end"
    (expect-stream-events
      (b/later 1 "hipsta!")
      "hipsta!")))

(defasync sequentially
  (testing "it should send events and end"
    (expect-stream-events
      (b/sequentially 1 ["hipsta 1" "hipsta 2"])
      "hipsta 1" "hipsta 2")))

(defasync from-array
  (testing "it should send all events and end immediately"
    (expect-stream-events
      (b/from-array ["I've" "got" "a" "lovely"])
      "I've" "got" "a" "lovely")))

(defasync merging
  (testing "it should be mergable"
    (expect-stream-events
      (-> (b/from-array [1 2 3 4])
          (b/merge (b/from-array [5 6 7 8])))
      5 6 7 8 1 2 3 4)))

(defasync property
  (testing "delivers current value and changes"
    (expect-property-events
      (-> (b/later 5 "b")
          (b/merge (b/from-array ["a"]))
          (b/to-property))
      "a" "b")))

(defasync mapping
  (testing "it should map values"
    (expect-stream-events
      (-> (b/from-array [1 2 3])
          (b/map inc))
      2 3 4)))

(defasync filtering
  (testing "it should filter values"
    (expect-stream-events
      (-> (b/from-array ["a" "b" "c"])
          (b/filter (partial not= "c")))
      "a" "b")))

(defasync filter-and-map
  (testing "it should be composable"
    (expect-stream-events
      (-> (b/from-array [1 2 3 4])
          (b/filter even?)
          (b/map inc))
      3 5)))

(defasync taking-n
  (testing "it takes the first n values"
    (expect-stream-events
      (-> (b/from-array [1 2 3 4 5])
          (b/take 3))
      1 2 3)))

(defasync taking-while
  (testing "it should take while"
    (expect-stream-events
      (-> (b/from-array [1 2 3 4])
          (b/take-while (partial > 3)))
      1 2)))
