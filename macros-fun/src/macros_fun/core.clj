(ns macros-fun.core)

(defmacro unless [condition & body]
  "Execute body when condition is false"
  `(if (not ~condition)
     (do ~@body)))

(defmacro debug [expr]
  "Print expression and its value, then return the value"
  `(let [result# ~expr]
     (println "Debug:" '~expr "=" result#)
     result#))

(defmacro infix [expr]
  "Allow infix notation for binary operations"
  (let [[left op right] expr]
    `(~op ~left ~right)))

(defmacro twice [expr]
  "Evaluate expression once, add it to itself"
  `(let [x# ~expr]
     (+ x# x#)))