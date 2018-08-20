(ns cell.core)


(require '[clojure.string :as str])
(use 'clojure.walk)

(def buf (atom {}))

(def stack-sign (atom []))

(defn tpos? []
  "Current state of sign"
  (even? (count (filter #(= % "-") @stack-sign))))

(defn to-map [coef var]
  "Add coeficient to vars"
  (println ">> DEBUG to map: " var coef)
  (def v (cond
           (nil? var) (:_const)
            :else  (keyword var)))
  (let [s  (get @buf v [])]
    (println ">> DEBUG to map inner: " s)
    (swap! buf assoc v (conj s coef)))
  (println ">> DEBUG to map after: " @buf)
  )

(defn remove-parens [s]
  "Unwrap parens from expression using stack"
  (map #(case %
          ("-" "+") (swap! stack-sign conj %)
          "(" ()
          ")" (if (seq? @stack-sign)
                (swap! stack-sign pop))
          (let [[_ c v] (re-matches #"(\d+)([a-z])?" %)]
            (if (tpos?)
              (to-map (Integer. c) v)
              (to-map (* -1 (Integer. c)) v))
            (if (seq @stack-sign)
              (swap! stack-sign pop))))
       s))

(defn parse [s]
  "Parse a string into a list of numbers, ops, and lists"
  (-> (str/trim s)
      (.replaceAll "([+-])" " $1 ")
      (str/split #"\s+")
      remove-parens))

(defn simplify []
  "Calculate coeficients"
  (doseq [[k v] @buf] (swap! buf assoc k (reduce + v))))

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
  (println ">>> DEBUG coeficient map:" c)
  (simplify)
  (let [res (fmt (stringify @buf))]
    (println ">>>>>>>>>>>>> DEBUG str:" res)
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