(ns vectors-fun.core)

(defn -main []
  (println "\n=== VECTORS ===\n")

  ;; Create
  (def v [1 2 3 4 5])
  (println "v =" v)

  ;; Access
  (println "(v 2)       =" (v 2))  ; index 2
  (println "(first v)   =" (first v))
  (println "(last v)    =" (last v))

  ;; Add/Remove
  (println "(conj v 6)  =" (conj v 6))  ; add to end
  (println "(pop v)     =" (pop v))     ; remove from end

  ;; Update
  (println "(assoc v 2 99) =" (assoc v 2 99))  ; replace index 2

  ;; Slice
  (println "(subvec v 1 4) =" (subvec v 1 4))  ; [start end)

  ;; Persistence
  (println "\nv is still   =" v "(unchanged!)")

  (println "\n=== DONE ===\n"))
