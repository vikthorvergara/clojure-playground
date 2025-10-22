(ns macros-fun.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [macros-fun.core :refer [unless debug infix twice]]))

(deftest unless-test
  (testing "unless executes when condition is false"
    (is (= 42 (unless false 42)))
    (is (nil? (unless true 42)))))

(deftest debug-test
  (testing "debug prints and returns value"
    (is (= 3 (debug (+ 1 2))))))

(deftest infix-test
  (testing "infix notation works"
    (is (= 8 (infix (5 + 3))))
    (is (= 15 (infix (5 * 3))))
    (is (= 2 (infix (5 - 3))))))

(deftest twice-test
  (testing "twice evaluates expression once and doubles it"
    (is (= 10 (twice 5)))
    (is (= 6 (twice (+ 1 2))))))

(deftest macro-hygiene-test
  (testing "macros don't leak variables"
    (let [x# 100]
      (is (= 10 (twice 5))))))
