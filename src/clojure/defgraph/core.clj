(ns defgraph.core
  (:gen-class)
  (:refer-clojure :exclude [parents]))

(def defs     (filter #(.isFile %) (.listFiles (java.io.File. "defs/runs"))))
(def vertices (map #(.getName %) defs))
(def yaml     (org.yaml.snakeyaml.Yaml. (ExtendedConstructor.)))
(def extends  (fn [x] (.get (.load yaml (slurp (.getPath x))) "ddts_extends")))
(def parents  (map extends defs))
(def edges    (into {} (filter val (zipmap vertices parents))))
(def rootpath (memoize #(let [x (edges %)] (if x (conj {% x} (rootpath x)) {}))))

(defn graph [re]
  (let [filtered-edges (filter #(re-matches re (first %)) edges)]
    {:V (set vertices)
     :E (reduce conj {} (map #(rootpath %) (keys filtered-edges)))}))

(defn -main [& args]
  (if (> (count args) 1)
    (println "Supply at most a single filtering prefix.")
    (let [g (graph (re-pattern (str (first args) ".*")))]
      (println g))))
