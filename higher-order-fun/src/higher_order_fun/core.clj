(ns higher-order-fun.core)

(defn -main []
  (println "\n=== HIGHER-ORDER FUNCTIONS ===\n")

  (def nums [1 2 3 4 5])
  (println "nums =" nums)

  ;; map - transform each element
  (println "\n(map inc nums)    =" (map inc nums))
  (println "(map #(* % 2) nums) =" (map #(* % 2) nums))

  ;; filter - keep elements that match
  (println "\n(filter even? nums)  =" (filter even? nums))
  (println "(filter odd? nums)   =" (filter odd? nums))
  (println "(filter #(> % 2) nums)=" (filter #(> % 2) nums))

  ;; reduce - combine elements
  (println "\n(reduce + nums)      =" (reduce + nums))
  (println "(reduce * nums)      =" (reduce * nums))
  (println "(reduce max nums)    =" (reduce max nums))

  ;; comp - compose functions (right to left)
  (def double-and-inc (comp inc #(* % 2)))
  (println "\n(double-and-inc 5)   =" (double-and-inc 5))
  (println "(map (comp inc #(* % 2)) nums) =" (map (comp inc #(* % 2)) nums))

  ;; partial - partially apply function
  (def add10 (partial + 10))
  (println "\n(add10 5)            =" (add10 5))
  (println "(map (partial * 2) nums) =" (map (partial * 2) nums))

  ;; apply - call function with collection as args
  (println "\n(apply + nums)       =" (apply + nums))
  (println "(apply max nums)     =" (apply max nums))

  ;; Combining them all
  (println "\nChaining:")
  (println "(->> nums")
  (println "     (filter odd?)")
  (println "     (map #(* % 2))")
  (println "     (reduce +))     ="
           (->> nums
                (filter odd?)
                (map #(* % 2))
                (reduce +)))

  (println "\n=== DONE ===\n"))
