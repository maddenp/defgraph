(ns confgraph.core
  (:gen-class)
  (:import ExtendedConstructor
           java.io.File
           javax.swing.JFrame
           org.jgraph.JGraph
           [org.jgraph.graph DefaultGraphCell GraphConstants]
           org.jgrapht.ext.JGraphModelAdapter
           [org.jgrapht EdgeFactory ListenableGraph]
           [org.jgrapht.graph DefaultEdge ListenableDirectedGraph]
           org.yaml.snakeyaml.Yaml)
  (:refer-clojure :exclude [parents]))

(def defs     (filter #(.isFile %) (.listFiles (File. "defs/runs"))))
(def vertices (map #(.getName %) defs))
(def yaml     (Yaml. (ExtendedConstructor.)))
(def extends  (fn [x] (.get (.load yaml (slurp (.getPath x))) "ddts_extends")))
(def parents  (map extends defs))
(def edges    (into {} (filter val (zipmap vertices parents))))
(def rootpath (memoize #(let [x (edges %)] (if x (conj {% x} (rootpath x)) {}))))

(defn graph [re]
  (let [g (ListenableDirectedGraph. DefaultEdge)
        e (filter #(re-matches re (first %)) edges)]
    (doseq [[a b] (into {} (for [[a b] e] (rootpath a)))]
      (doto g (.addVertex a) (.addVertex b) (.addEdge a b)))
    g))

(defn -main [& args]
  (if (> (count args) 1)
    (println "Supply at most a single filtering prefix.")
    (let [g (graph (re-pattern (str (first args) ".*")))
          adapter (JGraphModelAdapter. g)
          jgraph (JGraph. adapter)]
      (doto (JFrame. "TITLE")
        (.setSize 800 600)
        (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
        (.add jgraph)
        (.setVisible true)))))
