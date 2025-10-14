(ns bitonic.core-test
  (:require [clojure.test :refer :all]
            [bitonic.core :refer :all]
            [bitonic.db :as db]))

;; Initialize DB once before running tests
(use-fixtures :once
  (fn [f]
    (db/init-db!)
    (f)))

;; Helper to run an assertion and store result in DB
(defn record-test
  "Runs an assertion, logs result in DB, and returns the assertion result."
  [name expected actual]
  (let [status (if (= expected actual) "PASS" "FAIL")]
    (db/insert-result! name status expected actual)
    (is (= expected actual))))

;; --- Tests ---
(deftest bitonic-array-tests
  (testing "basic valid cases"
    (record-test "basic-1" [7 8 9 10 9] (bitonic-array 5 7 10))
    (record-test "basic-2" [2 3 4 5 4 3 2] (bitonic-array 7 2 5))
    (record-test "basic-3" [2 3 4 5] (bitonic-array 4 2 5))
    (record-test "basic-4" [1 2 3 4 3 2 1] (bitonic-array 7 1 4))
    (record-test "basic-5" [5 6 5] (bitonic-array 3 5 6)))

  (testing "edge ranges"
    (record-test "edge-1" 9 (count (bitonic-array 9 1 5)))
    (record-test "edge-2" [2 3 2] (bitonic-array 3 2 3))
    (record-test "edge-3" [5] (bitonic-array 1 5 5))
    (record-test "edge-4" [5 6] (bitonic-array 2 5 6))
    (record-test "edge-5" [4 5 6 5 4] (bitonic-array 5 4 6)))

  (testing "impossible cases"
    (record-test "impossible-1" [-1] (bitonic-array 15 1 5))
    (record-test "impossible-2" [-1] (bitonic-array 20 10 12))
    (record-test "impossible-3" [-1] (bitonic-array 10 1 3))
    (record-test "impossible-4" [-1] (bitonic-array 0 1 5))
    (record-test "impossible-5" [-1] (bitonic-array 5 10 9)))

  (testing "peak and range checks"
    (let [arr3 (bitonic-array 5 2 6)] ;=> [2 3 4 5 6]
      (record-test "peak-check" 6 (apply max arr3))
      (record-test "range-check" true (every? #(<= 2 % 6) arr3)))))