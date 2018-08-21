(ns cell.core)


(require '[clojure.string :as str])
(use 'clojure.walk)


;(def stack-sign (atom []))

(defn tpos? [s]
  "Current state of sign"
  (even? (count (filter #(= % "-") s))))

(defn to-map [coef var m]
  "Add coefficient to vars"
  (println ">> DEBUG to map: " var coef)
  (def v (cond
           (nil? var) (keyword "_const")
            :else  (keyword var)))
  (update m v #())
  ;(let [s  (get m v [])]
  ;  (println ">> DEBUG to map inner: " v s)
  ;  (assoc m v (conj s coef)))
  (println ">> DEBUG to map after: " m))

(defn convert [s]
  "Converts splitted expression into map {:var [coef1 coef2 ...]}.
  Solve sign of coefficients using stack"
  (def stack-sign [])
  (reduce (fn [m c] (case c
          ("-" "+") (conj stack-sign c)
          "(" ()
          ")" (if (seq? stack-sign)
                (pop stack-sign))
          (let [[_ c v] (re-matches #"(\d+)([a-z])?" c)]
            (if (tpos? stack-sign)
              (to-map (Integer. c) v m)
              (to-map (* -1 (Integer. c)) v m))
            (if (seq stack-sign)
              (pop stack-sign )))))
          {} s))

(defn parse [s]
  "Parse a string into map of variables and coefficients"
  (-> (str/trim s)
      (.replaceAll "([+-])" " $1 ")
      (str/split #"\s+")
      convert))

(defn simplify [m]
  "Calculate coefficients"
  (doseq [[k v] m] (assoc m k (reduce + v)))
  m)

(defn stringify [m]
  "Construct string expression"
  (let [res (reduce-kv (fn [acc k v]
                  (let [s (str/join "" [v (name k)])]
                    (if (pos? v)
                      (if (empty? acc)
                        (str/join ""  [acc s])
                        (str/join "" [acc "+" s]))
                      (str/join ""  [acc s]))
                    ))

        "" (into (sorted-map) m))]
    res))

(defn fmt [s]
  "Apply formating rules"
  (-> (str/trim s)
      (.replaceAll "([+-])" " $1 ")))

(defn evaluate [s]
  "Parse and evaluate an infix arithmetic expression"
  ;(stringify (simplify (parse s))))
  (def c (parse s))
  ;(println ">>> DEBUG coeficient map:" c)
  (def m (simplify c))
  (let [res (fmt (stringify m))]
    ;(println ">>>>>>>>>>>>> DEBUG str:" res)
    res))

(defn -main
  "Read from STDIN"
  [& args]

  ;(loop [i 0]
  ;  (when (< i n)
  ;    (def a (read-line))
  ;    (def new (split a #"\s+"))
  ;    (println ( + (Integer/parseInt (get new 0)) (Integer/parseInt (get new 1)) ))
  ;    (recur (inc i))
  ;    ))

  (loop [input (read-line)]
    (if (= ":done" input)
      (println (evaluate input))
      (recur (read-line)))))