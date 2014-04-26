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
