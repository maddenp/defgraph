(ns confgraph.core
  (:gen-class)
  (:import ExtendedConstructor)
  (:import java.io.File)
  (:import org.yaml.snakeyaml.Yaml)
  (:import (org.jgrapht EdgeFactory))
  (:import (org.jgrapht.graph DefaultDirectedGraph DefaultEdge)))

(def yaml     (Yaml. (ExtendedConstructor.)))
(def extends  (memoize (fn [d] (.get (.load yaml (slurp (.getPath d))) "ddts_extends"))))
(def defs     (for [x (.listFiles (File. "defs/runs")) :when (.isFile x)] x))
(def vertices (for [x defs] (.getName x)))
(def edges    (zipmap vertices (for [x defs] (extends x))))
;;(def edges    (filter #(val %) (zipmap vertices (for [x defs] (extends x)))))

(def rootpath
  (memoize (fn [src]
             (let [dst (edges src)]
               (if (nil? dst)
                 {}
                 (conj {src dst} (rootpath dst)))))))

(defn filtered-edges [prefix]
  (let [re (re-pattern (str prefix ".*"))]
    (into {} (for [[src dst] edges :when (re-matches re src)] (rootpath src)))))

(defn usage []
  (println "Supply at most a single filtering prefix.")
  (System/exit 1))

(defn -main [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (if (> (count args) 1) (usage))
  (let [fe (filtered-edges (first args))
        g (DefaultDirectedGraph. DefaultEdge)]
    (doseq [v vertices] (. g (addVertex v)))
    (doseq [e fe] (. g (addEdge (first e) (last e))))
    (println (.toString g))))
