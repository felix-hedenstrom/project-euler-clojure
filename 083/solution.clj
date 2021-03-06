(require '[clojure.string :as str])
(require '[clojure.pprint :as pp])
(load-file "../graph.clj")

(let [g (-> (create-graph)
            (add-edge :start [0 0] 131)
            (add-edge [0 0] [0 1] 673)
            (add-edge [0 1] [0 0] 131)
            (add-edge [0 0] [1 0] 201)
            (add-edge [1 0] [0 0] 131)
            (add-edge [1 1] [1 0] 201)
            (add-edge [1 0] [1 1] 96)
            (add-edge [1 1] [0 1] 673)
            (add-edge [0 1] [1 1] 96))])

(def all-directions {:allow-up true :allow-down true :allow-left true :allow-right true})

; Reduces the problem to a djiksta problem where :start is the staring point and :end is the end point
(defn text-to-graph
  ([text allowed-directions]
    (text-to-graph text allowed-directions  #"\s"))
  (
  [text {allow-up :allow-up allow-down :allow-down allow-left :allow-left allow-right :allow-right} splitregex]
  (let [add-points (fn [graph grid p1 p2]
                     (-> (add-edge graph p1 p2 (get-in grid p2)) 
                         (add-edge p2 p1 (get-in grid p1))))
        grid
        (->> (str/split-lines text)
             (map str/trim)
             (map #(str/split % splitregex))
             (map (fn [line] (vec (map #(Integer/parseInt %) line))))
             vec)
        height (count grid)
        width (count (first grid))]
    (-> (loop [x-range (range width)
           y-range (range height)
           graph (create-graph)]
      (cond
        (empty? y-range)
          graph
        (empty? x-range)
          (recur (range width) (rest y-range) graph)
        :else
          (let [x (first x-range)
                y (first y-range)
                p1 [y x]]
            (as-> graph graph
              ; Up
              (if (or (zero? y) (not allow-up))
                graph
                (add-edge graph p1 [(dec y) x] (get-in grid [(dec y) x])))
              ; Down
              (if (or (= (dec height) y) (not allow-down))
                graph
                (add-edge graph p1 [(inc y) x] (get-in grid [(inc y) x])))
              ; Left 
              (if (or (zero? x) (not allow-left))
                graph
                (add-edge graph p1 [y (dec x)] (get-in grid [y (dec x)])))
              ; Down
              (if (or (= (dec width) x) (not allow-down))
                graph
                (add-edge graph p1 [y (inc x)] (get-in grid [y (inc x)])))
              (recur (rest x-range) y-range graph)))))
      (add-edge :start [0 0] (get-in grid [0 0]))
      (add-edge [(dec height) (dec width)] :end 0)))))

(def g1 "2 4 8
        16 32 64
        128 256 512")

(def g2 "131 673 234 103 18
         201 96 342 965 150
         630 803 746 422 111
         537 699 497 121 956
         805 732 524 37 331") 

; Test their example
(let [[distance previous] (dijkstra (text-to-graph g2 all-directions) :start)] 
  (assert (= 2297 (:end distance))))

(let [text (slurp "matrix.txt")
      graph (text-to-graph g2 {:allow-right true :allow-down true :allow-up false :allow-left false} #"\s") 
      [distance previous] (dijkstra graph :start)] 
  (assert (= 2427 (:end distance))))

(comment
; 81
(let [text (slurp "matrix.txt")
      graph (text-to-graph text {:allow-right true :allow-down true :allow-up false :allow-left false} #",") 
      [distance previous] (dijkstra graph :start)] 
  (println "81: " (:end distance))))

; 83
(let [[distance previous] (dijkstra (text-to-graph (slurp "matrix.txt") all-directions #",") :start)] 
  (println "83: " (:end distance)))

