(ns bitonic-benchmark
  (:require [criterium.core :as crit]
            [bitonic.core :as bitonic]))

;; ============================================================================
;; Benchmark Scenarios
;; ============================================================================

(defn benchmark-small-arrays
  "Benchmark small bitonic arrays (n <= 10)"
  []
  (println "\n=== Benchmarking Small Arrays (n=5, range 1-10) ===\n")
  (crit/bench (bitonic/bitonic-array 5 1 10)))

(defn benchmark-medium-arrays
  "Benchmark medium bitonic arrays (n <= 100)"
  []
  (println "\n=== Benchmarking Medium Arrays (n=50, range 1-100) ===\n")
  (crit/bench (bitonic/bitonic-array 50 1 100)))

(defn benchmark-large-arrays
  "Benchmark large bitonic arrays (n <= 1000)"
  []
  (println "\n=== Benchmarking Large Arrays (n=500, range 1-1000) ===\n")
  (crit/bench (bitonic/bitonic-array 500 1 1000)))

(defn benchmark-very-large-arrays
  "Benchmark very large bitonic arrays (n <= 10000)"
  []
  (println "\n=== Benchmarking Very Large Arrays (n=5000, range 1-10000) ===\n")
  (crit/bench (bitonic/bitonic-array 5000 1 10000)))

(defn benchmark-edge-cases
  "Benchmark edge cases"
  []
  (println "\n=== Benchmarking Edge Cases ===\n")

  (println "Empty array (n=0):")
  (crit/bench (bitonic/bitonic-array 0 1 10))

  (println "\nInvalid range (l > r):")
  (crit/bench (bitonic/bitonic-array 5 10 1))

  (println "\nMinimum valid array (n=1):")
  (crit/bench (bitonic/bitonic-array 1 1 10))

  (println "\nMaximum length array (n=2*(r-l)+1):")
  (crit/bench (bitonic/bitonic-array 19 1 10)))

(defn benchmark-different-ranges
  "Benchmark with different range sizes for fixed n"
  []
  (println "\n=== Benchmarking Different Range Sizes (n=100) ===\n")

  (println "Small range (1-10):")
  (crit/bench (bitonic/bitonic-array 100 1 10))

  (println "\nMedium range (1-100):")
  (crit/bench (bitonic/bitonic-array 100 1 100))

  (println "\nLarge range (1-1000):")
  (crit/bench (bitonic/bitonic-array 100 1 1000))

  (println "\nVery large range (1-10000):")
  (crit/bench (bitonic/bitonic-array 100 1 10000)))

(defn benchmark-increasing-vs-decreasing
  "Benchmark increasing vs decreasing portion dominance"
  []
  (println "\n=== Benchmarking Increasing vs Decreasing Portions ===\n")

  (println "Mostly increasing (n=5, range 1-100):")
  (crit/bench (bitonic/bitonic-array 5 1 100))

  (println "\nBalanced (n=100, range 1-100):")
  (crit/bench (bitonic/bitonic-array 100 1 100))

  (println "\nMostly decreasing (n=195, range 1-100):")
  (crit/bench (bitonic/bitonic-array 195 1 100)))

(defn benchmark-implementation-analysis
  "Analyze the performance of individual operations in the algorithm"
  []
  (println "\n=== Implementation Analysis ===\n")

  (println "Testing (range) operation overhead:")
  (crit/bench
    (let [l 1
          r 1000
          p r
          inc-len 500]
      (vec (range (- p (dec inc-len)) (inc p)))))

  (println "\nTesting (iterate + take) operation overhead:")
  (crit/bench
    (let [p 1000
          dec-len 500]
      (vec (take dec-len (iterate dec (dec p))))))

  (println "\nTesting (concat) operation overhead:")
  (crit/bench
    (let [inc-part (range 1 501)
          dec-part (take 500 (iterate dec 999))]
      (vec (concat inc-part dec-part)))))

;; ============================================================================
;; Comparison with Alternative Implementations
;; ============================================================================

(defn bitonic-array-alternative-1
  "Alternative implementation using loop/recur for decreasing part"
  [n l r]
  (if (or (<= n 0) (> l r))
    [-1]
    (let [max-len (+ (* 2 (- r l)) 1)]
      (if (> n max-len)
        [-1]
        (let [p r
              inc-len (min n (inc (- p l)))
              dec-len (- n inc-len)
              inc-part (range (- p (dec inc-len)) (inc p))
              dec-part (loop [i 0
                              acc []]
                         (if (< i dec-len)
                           (recur (inc i) (conj acc (- p i 1)))
                           acc))]
          (vec (concat inc-part dec-part)))))))

(defn bitonic-array-alternative-2
  "Alternative implementation building result directly with into"
  [n l r]
  (if (or (<= n 0) (> l r))
    [-1]
    (let [max-len (+ (* 2 (- r l)) 1)]
      (if (> n max-len)
        [-1]
        (let [p r
              inc-len (min n (inc (- p l)))
              dec-len (- n inc-len)
              inc-part (range (- p (dec inc-len)) (inc p))
              dec-part (take dec-len (iterate dec (dec p)))]
          (into [] (concat inc-part dec-part)))))))

(defn benchmark-alternative-implementations
  "Compare different implementation approaches"
  []
  (println "\n=== Comparing Alternative Implementations ===\n")

  (println "Original implementation (using vec + concat):")
  (crit/bench (bitonic/bitonic-array 100 1 100))

  (println "\nAlternative 1 (using loop/recur for decreasing part):")
  (crit/bench (bitonic-array-alternative-1 100 1 100))

  (println "\nAlternative 2 (using into instead of vec):")
  (crit/bench (bitonic-array-alternative-2 100 1 100)))

;; ============================================================================
;; Memory Profiling
;; ============================================================================

(defn memory-usage-test
  "Test memory allocations for different array sizes"
  []
  (println "\n=== Memory Usage Analysis ===\n")
  (println "Note: Use JVM profiling tools (e.g., VisualVM, YourKit) for detailed memory analysis")
  (println "This section shows execution time which correlates with allocation overhead\n")

  (doseq [n [10 100 1000 10000]]
    (println (str "Array size n=" n ":"))
    (crit/quick-bench (bitonic/bitonic-array n 1 n))))

;; ============================================================================
;; Main Entry Point
;; ============================================================================

(defn run-all-benchmarks
  "Run all bitonic algorithm benchmarks"
  []
  (println "==========================================================")
  (println "Bitonic Array Generation Performance Benchmarks")
  (println "==========================================================")
  (println "Using Criterium for statistical analysis")
  (println "==========================================================")

  (benchmark-small-arrays)
  (benchmark-medium-arrays)
  (benchmark-large-arrays)
  (benchmark-very-large-arrays)
  (benchmark-edge-cases)
  (benchmark-different-ranges)
  (benchmark-increasing-vs-decreasing)
  (benchmark-implementation-analysis)
  (benchmark-alternative-implementations)
  (memory-usage-test)

  (println "\n==========================================================")
  (println "Benchmarks complete!")
  (println "==========================================================")
  (println "\nInterpretation Guide:")
  (println "- Execution time: Look at 'Execution time mean' for average performance")
  (println "- Variance: Lower is better (indicates consistent performance)")
  (println "- Overhead: GC and JIT overhead shown separately")
  (println "- Outliers: High outlier percentages indicate inconsistent performance"))

(defn run-quick-benchmarks
  "Run a quick subset of benchmarks for rapid iteration"
  []
  (println "==========================================================")
  (println "Quick Bitonic Benchmarks (Subset)")
  (println "==========================================================\n")

  (println "Small array:")
  (crit/quick-bench (bitonic/bitonic-array 5 1 10))

  (println "\nMedium array:")
  (crit/quick-bench (bitonic/bitonic-array 50 1 100))

  (println "\nLarge array:")
  (crit/quick-bench (bitonic/bitonic-array 500 1 1000))

  (println "\n=========================================================="))

(defn -main
  "Main entry point for running benchmarks"
  [& args]
  (if (some #{"--quick"} args)
    (run-quick-benchmarks)
    (run-all-benchmarks)))
