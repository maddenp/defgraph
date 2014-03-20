(ns confgraph.core
  (:gen-class)
  (:import ExtendedConstructor)
  (:import java.io.File)
  (:import org.yaml.snakeyaml.Yaml)
  (:use lacij.edit.graph
        lacij.layouts.core
        lacij.layouts.layout
        lacij.view.graphview))

(def  yaml (Yaml. (ExtendedConstructor.)))
(defn extends [conf] (.get (.load yaml (slurp (.getPath conf))) "extends"))
(def  confs (for [x (.listFiles (File. "configs/runs")) :when (.isFile x)] x))
(def  nodes (for [x confs] (.getName x)))
(def  edges (zipmap nodes (for [x confs] (extends x))))

(def rootpath
  (memoize (fn [src]
             (let [dst (edges src)]
               (if (nil? dst)
                 {}
                 (conj {src dst} (rootpath dst)))))))

(defn filtered-edges [prefix]
  (let [re (re-pattern (str prefix ".*"))]
    (into {} (for [[src dst] edges :when (re-matches re src)] (rootpath src)))))

(defn add-nodes [g & ns]
  (reduce
   (fn [g n]
     (let [k (keyword n) v (name n) w (* 8 (count v))]
       (add-node g k v :height 20 :width w :style {:stroke "none"} )))
   g
   ns))

(defn add-edges [g & e]
  (let [g (apply add-nodes g (set (flatten e)))]
    (reduce
     (fn [g [src dst]]
       (let [id (keyword (str (name src) "-" (name dst)))]
         (add-edge g id (keyword src) (keyword dst) :style {:stroke "grey"})))
     g
     (first e))))

(defn usage []
  (println "Supply at most a single filtering prefix.")
  (System/exit 1))

(defn -main [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (if (> (count args) 1) (usage))
  (-> (graph)
      (add-edges (into [] (filtered-edges (first args))))
      (layout :hierarchical)
      (build)
      (export "graph.svg")))
