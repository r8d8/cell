(ns cell.core-test
  (:require [clojure.test :refer :all]
            [cell.core :refer :all]))


(deftest simplify-test
  (testing "FIXME, I fail."
    (are [test-in simplified]
      (= simplified (evaluate test-in))
    ; "2 * 6 - (13 + (5 * 3)) / 4" "5")))
    "2x + 6y - (12 + (5x - 3y)) + 4" "- 3x + 9y - 8")))
  ; (println (evaluate "2 * 6 - (13 + (5 * 3)) / 4")))