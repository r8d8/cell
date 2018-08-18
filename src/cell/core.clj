; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; UI ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; (load-file "/home/k2/Workshop/cell/src/cell/ant.clj")
; (import
;  '(java.awt Color Graphics Dimension)
;  '(java.awt.image BufferedImage)
;  '(javax.swing JPanel JFrame)); ;pixels per world cell
; (def scale 5)

; (defn fill-cell [#^Graphics g x y c]
;   (doto g
;     (.setColor c)
;     (.fillRect (* x scale) (* y scale) scale scale)))

; (defn render-ant [ant #^Graphics g x y]
;   (let [black (. (new Color 0 0 0 255) (getRGB))
;         gray (. (new Color 100 100 100 255) (getRGB))
;         red (. (new Color 255 0 0 255) (getRGB))
;         [hx hy tx ty] ({0 [2 0 2 4]
;                         1 [4 0 0 4]
;                         2 [4 2 0 2]
;                         3 [4 4 0 0]
;                         4 [2 4 2 0]
;                         5 [0 4 4 0]
;                         6 [0 2 4 2]
;                         7 [0 0 4 4]}
;                        (:dir ant))]
;     (doto g
;       (.setColor (if (:food ant)
;                    (new Color 255 0 0 255)
;                    (new Color 0 0 0 255)))
;       (.drawLine (+ hx (* x scale)) (+ hy (* y scale))
;                  (+ tx (* x scale)) (+ ty (* y scale))))))

; (defn render-place [g p x y]
; (defn render [g]
;   (let [v (dosync (apply vector (for [x (range dim) y (range dim)]
;                                   @(place [x y]))))
;         img (new BufferedImage (* scale dim) (* scale dim)
;                  (. BufferedImage TYPE_INT_ARGB))
;         bg (. img (getGraphics))]
;     (doto bg
;       (.setColor (. Color white))
;       (.fillRect 0 0 (. img (getWidth)) (. img (getHeight))))
;     (dorun
;      (for [x (range dim) y (range dim)]
;        (render-place bg (v (+ (* x dim) y)) x y)))
;     (doto bg
;       (.setColor (. Color blue))
;       (.drawRect (* scale home-off) (* scale home-off)
;                  (* scale nants-sqrt) (* scale nants-sqrt)))
;     (. g (drawImage img 0 0 nil))
;     (. bg (dispose))))

; (def panel (doto (proxy [JPanel] []
;                    (paint [g] (render g)))
;              (.setPreferredSize (new Dimension
;                                      (* scale dim)
;                                      (* scale dim)))))

; (def frame (doto (new JFrame) (.add panel) .pack .show))

; (def animator (agent nil))

; (defn animation [x]
;   (when running
;     (send-off *agent* #'animation))
;   (. panel (repaint))
;   (. Thread (sleep animation-sleep-ms))
;   nil)

; (def evaporator (agent nil))

; (defn evaporation [x]
;   (when running
;     (send-off *agent* #'evaporation))
;   (evaporate)
;   (. Thread (sleep evap-sleep-ms))
;   nil)

; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; use ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; ;; (comment
; ;demo
; (def ants (setup))
; (send-off animator animation)
; (dorun (map #(send-off % behave) ants))
; (send-off evaporator evaporation)

; ;; )
; (defmacro report
;   [to-try]
;   `(let [result# ~to-try]
;      (if result#
;        (println (quote ~to-try) "was successful:" result#)
;        (println (quote ~to-try) "was not successful:" result#))))

; (defn validate
;   "Returns a map with a vector of errors for each key"
;   [to-validate validations]
;   (reduce (fn [errors validation]
;             (let [[fieldname validation-check-groups] validation
;                   value (get to-validate fieldname)
;                   error-messages (error-messages-for value validation-check-groups)]
;               (if (empty? error-messages)
;                 errors
;                 (assoc errors fieldname error-messages))))
;           {}
;           validations))
; (validate order-details order-details-validations)



(ns cell.core)

; (defn compile-rule [rule]
;   (let [[pat alt] (unifier/prep rule)]
;      [(fn [expr] (logic/== expr pat))
;       (fn [sbst] (logic/== sbst alt))]))

; (defn raw-rule? [rule]
;   (not (vector? rule)))

; (defmacro defrules [name & rules]
;   `(let [rules# (for [rule# '~rules]
;                   (if (raw-rule? rule#)
;                     (eval rule#) ;; raw rule, no need to compile
;                     (compile-rule rule#)))]
;      (def ~name (vec rules#))))

; (defrules rules
;   [(+ ?x 1) (inc ?x)]
;   [(+ 1 ?x) (inc ?x)]
;   [(- ?x 1) (dec ?x)]

;   [(* ?x (* . ?xs)) (* ?x . ?xs)]
;   [(+ ?x (+ . ?xs)) (+ ?x . ?xs)]

;   ;;trivial identites
;   [(+ ?x 0) ?x]
;   [(- ?x 0) ?x]
;   [(* ?x 1) ?x]
;   [(/ ?x 1) ?x]
;   [(* ?x 0) 0]

; (defn simplify-one [expr rules]
;   (let [alts (logic/run* [q]
;                (logic/fresh [pat subst]
;                  (logic/membero [pat subst] rules)
;                  (logic/project [pat subst]
;                    (logic/all (pat expr)
;                               (subst q)))))]
;     (if (empty? alts) expr (first alts))))

;; Simplifies expr according to the rules until no more rules apply.
; (defn simplify [expr rules]
;   (->> expr
;        (iterate (partial walk/prewalk #(simplify-one % rules)))
;        (partition 2 1)
;        (drop-while #(apply not= %))
;        (ffirst)))

(def precedence '{* 0, / 0
                  + 1, - 1})

(def ops {'* *
          '+ +
          '- -
          '/ /})

(defn order-ops
  "((A x B) y C) or (A x (B y C)) depending on precedence of x and y"
  [[A x B y C & more]]
  (let [ret (if (<=  (precedence x)
                     (precedence y))
              (list (list A x B) y C)
              (list A x (list B y C)))]
    (if more
      (recur (concat ret more))
      ret)))

(defn add-parens
  "Tree walk to add parens.  All lists are length 3 afterwards."
  [s]
  (clojure.walk/postwalk
   #(if (seq? %)
      (let [c (count %)]
        (cond (even? c) (throw (Exception. "Must be an odd number of forms"))
              (= c 1) (first %)
              (= c 3) %
              (>= c 5) (order-ops %)))
      %)
   s))

(defn make-ast
  "Parse a string into a list of numbers, ops, and lists"
  [s]
  (-> (format "'(%s)" s)
      (.replaceAll , "([*+-/])" " $1 ")
      load-string
      add-parens))

(defn can_simplify?
  [s]

  )

(defn simplify-one
  [s]

  )

(def eval-ast
  (partial clojure.walk/postwalk
           #(if (can_simplify? %)
              (let [[a o b] %]
                ((ops o) a b))
              %)))

(defn evaluate [s]
  "Parse and evaluate an infix arithmetic expression"
  (eval-ast (make-ast s)))


(defn -main
  "Read from STDIN"
  [& args]

  (loop [input (read-line)]
    (if (= ":done" input)
      (println (evaluate input))
      (recur (read-line)))))