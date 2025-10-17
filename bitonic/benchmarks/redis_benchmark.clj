(ns redis-benchmark
  (:require [criterium.core :as crit]
            [taoensso.carmine :as car])
  (:import [redis.clients.jedis Jedis JedisPool JedisPoolConfig]
           [redis.clients.jedis.params XAddParams]))

;; ============================================================================
;; Configuration
;; ============================================================================

(def redis-host "localhost")
(def redis-port 6379)
(def test-stream "benchmark-test-results")

;; ============================================================================
;; Carmine Implementation (High-level Clojure client)
;; ============================================================================

(def carmine-conn
  {:pool {}
   :spec {:host redis-host
          :port redis-port
          :timeout-ms 4000}})

(defmacro wcar* [& body]
  `(car/wcar carmine-conn ~@body))

(defn carmine-single-write
  "Single XADD using Carmine"
  []
  (wcar*
    (car/xadd test-stream
              "*"
              "test_name" "bench-test"
              "status" "passed"
              "expected" "[1 2 3]"
              "actual" "[1 2 3]"
              "ts" (str (System/currentTimeMillis)))))

(defn carmine-batch-write
  "Pipelined batch XADD using Carmine wcar macro"
  [n]
  (wcar*
    (doall
      (for [i (range n)]
        (car/xadd test-stream
                  "*"
                  "test_name" (str "bench-test-" i)
                  "status" "passed"
                  "expected" "[1 2 3]"
                  "actual" "[1 2 3]"
                  "ts" (str (System/currentTimeMillis)))))))

(defn carmine-single-read
  "Single XREVRANGE using Carmine"
  []
  (wcar*
    (car/xrevrange test-stream "+" "-" "COUNT" 10)))

(defn carmine-cleanup
  "Delete the stream using Carmine"
  []
  (wcar* (car/del test-stream)))

;; ============================================================================
;; Jedis Implementation (Low-level Java client)
;; ============================================================================

(defonce jedis-pool
  (delay
    (let [config (JedisPoolConfig.)]
      (.setMaxTotal config 8)
      (.setMaxIdle config 8)
      (.setMinIdle config 0)
      (JedisPool. config redis-host redis-port))))

(defn jedis-single-write
  "Single XADD using Jedis"
  []
  (with-open [jedis (.getResource @jedis-pool)]
    (.xadd jedis
           test-stream
           (doto (java.util.HashMap.)
             (.put "test_name" "bench-test")
             (.put "status" "passed")
             (.put "expected" "[1 2 3]")
             (.put "actual" "[1 2 3]")
             (.put "ts" (str (System/currentTimeMillis))))
           (XAddParams/xAddParams))))

(defn jedis-batch-write
  "Batch XADD using Jedis with manual pipelining"
  [n]
  (with-open [jedis (.getResource @jedis-pool)]
    (let [pipeline (.pipelined jedis)]
      (dotimes [i n]
        (.xadd pipeline
               test-stream
               (doto (java.util.HashMap.)
                 (.put "test_name" (str "bench-test-" i))
                 (.put "status" "passed")
                 (.put "expected" "[1 2 3]")
                 (.put "actual" "[1 2 3]")
                 (.put "ts" (str (System/currentTimeMillis))))
               (XAddParams/xAddParams)))
      (.sync pipeline))))

(defn jedis-single-read
  "Single XREVRANGE using Jedis"
  []
  (with-open [jedis (.getResource @jedis-pool)]
    (.xrevrange jedis test-stream nil nil 10)))

(defn jedis-cleanup
  "Delete the stream using Jedis"
  []
  (with-open [jedis (.getResource @jedis-pool)]
    (.del jedis (into-array String [test-stream]))))

;; ============================================================================
;; Benchmark Runners
;; ============================================================================

(defn benchmark-single-writes
  "Compare single write performance"
  []
  (println "\n=== Benchmarking Single Writes (XADD) ===\n")

  (println "Carmine (high-level Clojure client):")
  (carmine-cleanup)
  (crit/bench (carmine-single-write))

  (println "\nJedis (low-level Java client):")
  (jedis-cleanup)
  (crit/bench (jedis-single-write)))

(defn benchmark-batch-writes
  "Compare batch write performance"
  [batch-size]
  (println (str "\n=== Benchmarking Batch Writes (" batch-size " operations) ===\n"))

  (println "Carmine (pipelined with wcar macro):")
  (carmine-cleanup)
  (crit/bench (carmine-batch-write batch-size))

  (println "\nJedis (manual pipelining):")
  (jedis-cleanup)
  (crit/bench (jedis-batch-write batch-size)))

(defn benchmark-single-reads
  "Compare single read performance"
  []
  (println "\n=== Benchmarking Single Reads (XREVRANGE) ===\n")

  ;; Seed data first
  (carmine-cleanup)
  (carmine-batch-write 100)

  (println "Carmine (high-level Clojure client):")
  (crit/bench (carmine-single-read))

  (println "\nJedis (low-level Java client):")
  (crit/bench (jedis-single-read)))

(defn benchmark-connection-overhead
  "Compare connection pool acquisition overhead"
  []
  (println "\n=== Benchmarking Connection Acquisition Overhead ===\n")

  (println "Carmine (connection from pool):")
  (crit/bench
    (wcar* (car/ping)))

  (println "\nJedis (connection from pool):")
  (crit/bench
    (with-open [jedis (.getResource @jedis-pool)]
      (.ping jedis))))

;; ============================================================================
;; Main Entry Point
;; ============================================================================

(defn run-all-benchmarks
  "Run all Redis client benchmarks"
  []
  (println "==========================================================")
  (println "Redis Client Performance Comparison: Carmine vs Jedis")
  (println "==========================================================")
  (println "Configuration:")
  (println (str "  Host: " redis-host))
  (println (str "  Port: " redis-port))
  (println (str "  Stream: " test-stream))
  (println "==========================================================")

  ;; Ensure Redis is available
  (try
    (wcar* (car/ping))
    (println "Redis connection verified ✓\n")
    (catch Exception e
      (println "ERROR: Cannot connect to Redis!")
      (println (.getMessage e))
      (System/exit 1)))

  ;; Run benchmarks
  (benchmark-connection-overhead)
  (benchmark-single-writes)
  (benchmark-batch-writes 10)
  (benchmark-batch-writes 100)
  (benchmark-single-reads)

  ;; Cleanup
  (carmine-cleanup)
  (println "\n==========================================================")
  (println "Benchmarks complete!")
  (println "=========================================================="))

(defn -main
  "Main entry point for running benchmarks"
  [& args]
  (run-all-benchmarks))
