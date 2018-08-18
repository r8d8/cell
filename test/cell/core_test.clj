(ns cell.core-test
  (:require [clojure.test :refer :all]
            [cell.core :refer :all]))


;(deftest simplify-seq-test
;  (testing
;    (are [test-in simplified]
;      (= simplified (simplify-seq test-in))
;      "1x + 6y " "1x + 6y"
;      "1x - 6x " "-5x"
;      "1x - 6y + 2z + 3 + 2z - 3 " "1x - 6y + 4z")))

(deftest simplify-test
  (testing
    (are [test-in simplified]
      (= simplified (evaluate test-in))
    ;"2 + 6 - (1 - 3) + 4" 8)))
    "2x + 6y - 1x" "1x + 6y")))