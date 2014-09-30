(ns confgraph.core
  (:gen-class)
  (:import ExtendedConstructor
           java.io.File
           org.jgrapht.EdgeFactory
           org.jgrapht.graph.DefaultDirectedGraph
           org.jgrapht.graph.DefaultEdge
           org.yaml.snakeyaml.Yaml))

(def defs       (filter #(.isFile %) (.listFiles (File. "defs/runs"))))
(def vertices   (map #(.getName %) defs))
(def yaml       (Yaml. (ExtendedConstructor.)))
(def extends    (fn [x] (.get (.load yaml (slurp (.getPath x))) "ddts_extends")))
(def prototypes (map extends defs))
(def raw-edges  (zipmap vertices prototypes))

(def rootpath
  (memoize #(let [dst (raw-edges %)]
              (if (nil? dst) {} (conj {% dst} (rootpath dst))))))

(defn edges [prefix]
  (let [e (filter #(re-matches (re-pattern (str prefix ".*")) (first %)) raw-edges)]
    (into {} (for [[src dst] e] (rootpath src)))))

(defn -main [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (if (> (count args) 1)
    (println "Supply at most a single filtering prefix.")
    (let [edges (edges (first args)) graph (DefaultDirectedGraph. DefaultEdge)]
      (doseq [v vertices] (. graph (addVertex v)))
      (doseq [[src dst] edges] (. graph (addEdge src dst)))
      (println (.toString (.edgeSet graph))))))
