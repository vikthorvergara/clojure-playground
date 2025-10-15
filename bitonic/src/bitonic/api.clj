(ns bitonic.api
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.coercion :as coercion]
            [reitit.coercion.spec :as spec-coercion]
            [muuntaja.core :as m]
            [ring.util.http-response :as response]
            [bitonic.core :as core]
            [bitonic.db :as db]
            [clojure.spec.alpha :as s])
  (:import (java.util UUID Date)))

;; Specs for request validation
(s/def ::length pos-int?)
(s/def ::min int?)
(s/def ::max int?)
(s/def ::limit (s/and pos-int? #(<= % 1000)))
(s/def ::offset (s/and int? #(>= % 0)))

;; Generate bitonic sequence endpoint
(defn generate-bitonic
  "Generate a new bitonic sequence and store result in Redis"
  [{{:keys [length min max]} :body-params}]
  (try
    (let [sequence (core/bitonic-array length min max)
          id (str (UUID/randomUUID))
          timestamp (str (Date.))]
      (if (= sequence [-1])
        (response/bad-request
          {:error "Invalid parameters"
           :message "Cannot generate bitonic sequence with given parameters"
           :details {:length length :min min :max max}})
        (do
          ;; Store in Redis as a test result
          (db/insert-result!
            (str "bitonic-" id)
            "generated"
            {:length length :min min :max max}
            sequence)
          (response/ok
            {:id id
             :sequence sequence
             :length (count sequence)
             :parameters {:min min :max max}
             :timestamp timestamp}))))
    (catch Exception e
      (response/internal-server-error
        {:error "Generation failed"
         :message (.getMessage e)}))))

;; Get test results from Redis
(defn get-test-results
  "Retrieve latest test results from Redis stream"
  [{{:keys [limit]} :query-params}]
  (try
    (let [limit (or limit 100)
          results (db/get-results limit)]
      (response/ok
        {:results results
         :count (count results)
         :total (db/get-stream-length)}))
    (catch Exception e
      (response/internal-server-error
        {:error "Failed to retrieve results"
         :message (.getMessage e)}))))

;; Get results count
(defn get-results-count
  "Get total count of test results in Redis"
  [_]
  (try
    (response/ok
      {:count (db/get-stream-length)})
    (catch Exception e
      (response/internal-server-error
        {:error "Failed to get count"
         :message (.getMessage e)}))))

;; Health check endpoint
(defn health-check
  "Check API and Redis health"
  [_]
  (try
    (let [redis-ok (db/init-db!)]
      (response/ok
        {:status "ok"
          :redis (if redis-ok "connected" "disconnected")
          :timestamp (str (Date.))}))
    (catch Exception e
      (response/service-unavailable
        {:status "error"
         :redis "error"
         :message (.getMessage e)
         :timestamp (str (Date.))}))))

;; Clear all results (useful for testing)
(defn clear-all-results
  "Clear all test results from Redis (testing only)"
  [_]
  (try
    (db/clear-results!)
    (response/ok
      {:message "All results cleared"
       :timestamp (str (Date.))})
    (catch Exception e
      (response/internal-server-error
        {:error "Failed to clear results"
         :message (.getMessage e)}))))

;; Routes definition
(def routes
  [["/api"
    ["/health"
     {:get {:handler health-check
            :summary "Health check endpoint"}}]

    ["/bitonic"
     ["/generate"
      {:post {:handler generate-bitonic
              :summary "Generate a new bitonic sequence"
              :parameters {:body {:length int?
                                  :min int?
                                  :max int?}}
              :responses {200 {:body {:id string?
                                      :sequence vector?
                                      :length int?
                                      :parameters map?
                                      :timestamp string?}}
                          400 {:body {:error string?
                                      :message string?}}}}}]]

    ["/results"
     ["" {:get {:handler get-test-results
                :summary "Get latest test results"
                :responses {200 {:body {:results vector?
                                        :count int?
                                        :total int?}}}}}]
     ["/count" {:get {:handler get-results-count
                      :summary "Get count of test results"
                      :responses {200 {:body {:count int?}}}}}]
     ["/clear" {:delete {:handler clear-all-results
                         :summary "Clear all results (testing only)"
                         :responses {200 {:body {:message string?
                                                 :timestamp string?}}}}}]]]])

;; Application with middleware
(def app
  (ring/ring-handler
    (ring/router
      routes
      {:data {:coercion spec-coercion/coercion
              :muuntaja m/instance
              :middleware [;; Content negotiation
                          muuntaja/format-middleware
                          ;; Coercion
                          coercion/coerce-exceptions-middleware
                          coercion/coerce-request-middleware
                          coercion/coerce-response-middleware]}})
    (ring/create-default-handler
      {:not-found (constantly (response/not-found {:error "Not found"}))
       :method-not-allowed (constantly (response/method-not-allowed {:error "Method not allowed"}))
       :not-acceptable (constantly (response/not-acceptable {:error "Not acceptable"}))})))
