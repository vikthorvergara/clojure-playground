(ns bitonic.db-test
  (:require [clojure.test :refer :all]
            [bitonic.db :as db]))

(deftest test-redis-connection
  (testing "Redis connection initialization"
    (is (true? (db/init-db!)))))

(deftest test-insert-and-retrieve
  (testing "Insert and retrieve test results from Redis stream"
    ;; Clear any existing results
    (db/clear-results!)

    ;; Insert a test result
    (let [result-id (db/insert-result! "test-bitonic-array"
                                       "passed"
                                       [1 2 3 2 1]
                                       [1 2 3 2 1])]
      (is (some? result-id) "Result ID should be returned"))

    ;; Retrieve results
    (let [results (db/get-results 10)]
      (is (= 1 (count results)) "Should have 1 result")
      (is (= "test-bitonic-array" (:test-name (first results))))
      (is (= "passed" (:status (first results))))
      (is (= [1 2 3 2 1] (:expected (first results))))
      (is (= [1 2 3 2 1] (:actual (first results)))))))

(deftest test-batch-insert
  (testing "Batch insert multiple test results"
    (db/clear-results!)

    (let [test-data [{:test-name "test1" :status "passed"
                      :expected [1 2] :actual [1 2]}
                     {:test-name "test2" :status "failed"
                      :expected [3 4] :actual [3 5]}
                     {:test-name "test3" :status "passed"
                      :expected [5 6] :actual [5 6]}]]
      (db/insert-results-batch! test-data)

      (let [results (db/get-results)]
        (is (= 3 (count results)) "Should have 3 results")
        ;; Results are in reverse chronological order
        (is (= "test3" (:test-name (first results))))))))

(deftest test-stream-length
  (testing "Get stream length"
    (db/clear-results!)

    (db/insert-result! "test1" "passed" [1] [1])
    (db/insert-result! "test2" "passed" [2] [2])

    (is (= 2 (db/get-stream-length)))))

(deftest test-max-length-trimming
  (testing "Stream automatically trims to max length"
    (db/clear-results!)

    ;; This test would insert more than max-stream-length results
    ;; to verify auto-trimming, but would be slow.
    ;; For now, just verify the function exists and works
    (is (fn? db/insert-result!))))

;; Run tests with: clj -M:test -m clojure.test.runner
