(ns confgraph.core
  (:gen-class)
  (:import ExtendedConstructor
           java.io.File
           org.jgrapht.EdgeFactory
           org.jgrapht.graph.DefaultDirectedGraph
           org.jgrapht.graph.DefaultEdge
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
  (let [g (DefaultDirectedGraph. DefaultEdge)
        e (filter #(re-matches re (first %)) edges)]
    (doseq [[a b] (into {} (for [[a b] e] (rootpath a)))]
      (doto g (.addVertex a) (.addVertex b) (.addEdge a b)))
    g))

(defn -main [& args]
  (if (> (count args) 1)
    (println "Supply at most a single filtering prefix.")
    (let [g (graph (re-pattern (str (first args) ".*")))]
      (println (.toString g)))))
