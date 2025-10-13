(ns bitonic.core)

(defn bitonic-array [n l r]
  (if (or (<= n 0) (> l r))
    [-1]
    (let [max-len (+ (* 2 (- r l)) 1)]
      (if (> n max-len)
        [-1]
        ;; always use peak = r
        (let [p r
              inc-len (min n (inc (- p l)))
              dec-len (- n inc-len)
              inc-part (range (- p (dec inc-len)) (inc p))
              dec-part (take dec-len (iterate dec (dec p)))]
          (vec (concat inc-part dec-part)))))))