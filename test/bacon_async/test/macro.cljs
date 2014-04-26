(ns bacon-async.test.macro
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing test-var done)]
                   [bacon-async.test.macro :refer (with-timeout later)])
  (:require [cemerick.cljs.test :as t]
            [bacon-async.core :as b]))

(enable-console-print!)

(defn verify-cleanup [-test-ctx src]
  (is -test-ctx (= (count (b/subscribers src)) 0) "Cleaning up")
  (done))

(defn verify-results [-test-ctx done? src events-found events-expected]
  (is -test-ctx (= events-found events-expected) "Checking results")
  (reset! done? true))

(defn verify-single-subscriber [-test-ctx src & events-expected]
  (let [done? (atom false)]
    (with-timeout 75 done?
      (let [events-found (atom [])]
        (b/subscribe!
          src
          (fn [event]
            (if (:end? event)
              (do
                (verify-results -test-ctx done? src @events-found (or events-expected []))
                (done))
              (swap! events-found conj (:value event)))))
        ;; TODO: Make this check done? and re-poll if necessary
        #_      (later 65
                     (verify-cleanup -test-ctx src))))))

