(ns run
  "Unified benchmark runner for all performance tests"
  (:require [redis-benchmark :as redis]
            [bitonic-benchmark :as bitonic]))

(defn print-separator
  []
  (println "\n\n")
  (println "##########################################################")
  (println "##########################################################")
  (println "\n\n"))

(defn run-redis-benchmarks
  "Run Redis client comparison benchmarks"
  []
  (println "Starting Redis benchmarks...")
  (redis/run-all-benchmarks)
  (print-separator))

(defn run-bitonic-benchmarks
  "Run bitonic algorithm benchmarks"
  [quick?]
  (println "Starting bitonic algorithm benchmarks...")
  (if quick?
    (bitonic/run-quick-benchmarks)
    (bitonic/run-all-benchmarks))
  (print-separator))

(defn print-help
  []
  (println "Benchmark Runner")
  (println "================")
  (println)
  (println "Usage: clj -M:benchmark -m run [options]")
  (println)
  (println "Options:")
  (println "  --redis         Run Redis client benchmarks only")
  (println "  --bitonic       Run bitonic algorithm benchmarks only")
  (println "  --quick         Run quick benchmarks (bitonic only)")
  (println "  --all           Run all benchmarks (default)")
  (println "  --help          Show this help message")
  (println)
  (println "Examples:")
  (println "  clj -M:benchmark -m run --redis")
  (println "  clj -M:benchmark -m run --bitonic --quick")
  (println "  clj -M:benchmark -m run --all"))

(defn -main
  [& args]
  (let [args-set (set args)
        show-help (or (contains? args-set "--help")
                      (contains? args-set "-h"))
        run-redis (or (contains? args-set "--redis")
                      (contains? args-set "--all")
                      (empty? args))
        run-bitonic (or (contains? args-set "--bitonic")
                        (contains? args-set "--all")
                        (empty? args))
        quick (contains? args-set "--quick")]

    (cond
      show-help
      (print-help)

      :else
      (do
        (println "==========================================================")
        (println "   BENCHMARK SUITE - Bitonic Array Generation Project")
        (println "==========================================================")
        (println)
        (println (str "Timestamp: " (java.time.Instant/now)))
        (println (str "JVM Version: " (System/getProperty "java.version")))
        (println (str "Clojure Version: " (clojure-version)))
        (println)
        (println "Benchmarks to run:")
        (when run-redis (println "  ✓ Redis client comparison (Carmine vs Jedis)"))
        (when run-bitonic (println (str "  ✓ Bitonic algorithm performance" (if quick " (quick mode)" ""))))
        (println "==========================================================")
        (print-separator)

        (when run-redis
          (run-redis-benchmarks))

        (when run-bitonic
          (run-bitonic-benchmarks quick))

        (println "\n\n")
        (println "==========================================================")
        (println "   ALL BENCHMARKS COMPLETED")
        (println "==========================================================")
        (println)
        (println "Summary:")
        (println "- Review the output above for detailed performance metrics")
        (println "- Look for 'Execution time mean' for average performance")
        (println "- Lower variance indicates more consistent performance")
        (println "- Check for outliers and GC overhead")
        (println)
        (println "Next steps:")
        (println "- Compare mean execution times between implementations")
        (println "- Investigate high-variance or high-outlier scenarios")
        (println "- Consider profiling with VisualVM for memory analysis")
        (println "==========================================================")))))
