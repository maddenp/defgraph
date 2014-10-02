(ns defgraph.core
  (:gen-class)
  (:import
   ExtendedConstructor
   java.io.File
   org.yaml.snakeyaml.Yaml
   )
  (:refer-clojure :exclude [parents]))

(def defs     (filter #(.isFile %) (.listFiles (File. "defs/runs"))))
(def vertices (map #(.getName %) defs))
(def yaml     (Yaml. (ExtendedConstructor.)))
(def extends  (fn [x] (.get (.load yaml (slurp (.getPath x))) "ddts_extends")))
(def parents  (map extends defs))
(def edges    (into {} (filter val (zipmap vertices parents))))
(def rootpath (memoize #(let [x (edges %)] (if x (conj {% x} (rootpath x)) {}))))

(defn filtered-edges [re]
  (filter #(re-matches re (first %)) edges))

(defn graph [re]
  (println)
  {:V (set vertices)
   :E (reduce conj {} (map #(rootpath %) (keys (filtered-edges re))))})

(defn -main [& args]
  (if (> (count args) 1)
    (println "Supply at most a single filtering prefix.")
    (let [g (graph (re-pattern (str (first args) ".*")))]
      (println g))))
