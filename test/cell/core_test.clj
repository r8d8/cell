(ns cell.core-test
  (:require [clojure.test :refer :all]
            [cell.core :refer :all]))


(deftest simplify-test
  (are [test-in simplified]
    (= (evaluate test-in) simplified)
    ;"1x - 6y + 2z + 2z" "1x - 6y + 4z"
    "2x + 6y - 1x" "1x + 6y"))