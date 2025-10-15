(ns bitonic.api-test
  (:require [clojure.test :refer :all]
            [bitonic.api :as api]
            [bitonic.db :as db]
            [muuntaja.core :as m]
            [clojure.data.json :as json]))

;; Helper to call handlers with mock requests
(defn mock-request
  ([method uri]
   (mock-request method uri nil nil))
  ([method uri body]
   (mock-request method uri body nil))
  ([method uri body query-params]
   {:request-method method
    :uri uri
    :body-params body
    :query-params query-params
    :headers {"content-type" "application/json"}}))

;; Helper to parse JSON response
(defn parse-response [response]
  (update response :body #(when % (json/read-str % :key-fn keyword))))

;; Initialize DB before tests
(use-fixtures :once
  (fn [f]
    (db/init-db!)
    (f)))

;; Clear results before each test
(use-fixtures :each
  (fn [f]
    (db/clear-results!)
    (f)))

(deftest test-health-check
  (testing "Health check endpoint returns ok status"
    (let [response (api/health-check {})]
      (is (= 200 (:status response)))
      (is (= "ok" (get-in response [:body :status])))
      (is (= "connected" (get-in response [:body :redis]))))))

(deftest test-generate-bitonic-valid
  (testing "Generate valid bitonic sequence"
    (let [response (api/generate-bitonic
                     {:body-params {:length 5 :min 1 :max 10}})]
      (is (= 200 (:status response)))
      (is (string? (get-in response [:body :id])))
      (is (= 5 (get-in response [:body :length])))
      (is (vector? (get-in response [:body :sequence])))
      (is (= {:min 1 :max 10} (get-in response [:body :parameters]))))))

(deftest test-generate-bitonic-invalid
  (testing "Generate bitonic with invalid parameters returns error"
    (let [response (api/generate-bitonic
                     {:body-params {:length 100 :min 1 :max 5}})]
      (is (= 400 (:status response)))
      (is (= "Invalid parameters" (get-in response [:body :error]))))))

(deftest test-get-results-empty
  (testing "Get results when none exist"
    (let [response (api/get-test-results {:query-params {}})]
      (is (= 200 (:status response)))
      (is (= 0 (get-in response [:body :count])))
      (is (= 0 (get-in response [:body :total]))))))

(deftest test-get-results-with-data
  (testing "Get results after generating sequences"
    ;; Generate a few sequences
    (api/generate-bitonic {:body-params {:length 3 :min 1 :max 5}})
    (api/generate-bitonic {:body-params {:length 5 :min 2 :max 8}})

    (let [response (api/get-test-results {:query-params {}})]
      (is (= 200 (:status response)))
      (is (= 2 (get-in response [:body :count])))
      (is (= 2 (get-in response [:body :total]))))))

(deftest test-get-results-with-limit
  (testing "Get results with limit parameter"
    ;; Generate 5 sequences
    (dotimes [_ 5]
      (api/generate-bitonic {:body-params {:length 3 :min 1 :max 5}}))

    (let [response (api/get-test-results {:query-params {:limit 3}})]
      (is (= 200 (:status response)))
      (is (= 3 (get-in response [:body :count])))
      (is (= 5 (get-in response [:body :total]))))))

(deftest test-results-count
  (testing "Get count of test results"
    ;; Generate some sequences
    (dotimes [_ 3]
      (api/generate-bitonic {:body-params {:length 3 :min 1 :max 5}}))

    (let [response (api/get-results-count {})]
      (is (= 200 (:status response)))
      (is (= 3 (get-in response [:body :count]))))))

(deftest test-clear-results
  (testing "Clear all results"
    ;; Generate some sequences
    (api/generate-bitonic {:body-params {:length 3 :min 1 :max 5}})

    ;; Verify they exist
    (is (= 1 (get-in (api/get-results-count {}) [:body :count])))

    ;; Clear
    (let [response (api/clear-all-results {})]
      (is (= 200 (:status response)))
      (is (= "All results cleared" (get-in response [:body :message]))))

    ;; Verify they're gone
    (is (= 0 (get-in (api/get-results-count {}) [:body :count])))))

(deftest test-sequence-stored-in-redis
  (testing "Generated sequences are stored in Redis"
    (let [response (api/generate-bitonic
                     {:body-params {:length 7 :min 2 :max 5}})
          id (get-in response [:body :id])
          generated-seq (get-in response [:body :sequence])]

      ;; Retrieve from Redis
      (let [results (db/get-results 1)
            latest (first results)]
        (is (= "generated" (:status latest)))
        (is (= generated-seq (:actual latest)))
        (is (= {:length 7 :min 2 :max 5} (:expected latest)))))))
