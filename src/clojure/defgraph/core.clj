(ns defgraph.core
  (:gen-class)
  (:import com.mxgraph.swing.mxGraphComponent
           com.mxgraph.view.mxGraph
           javax.swing.JFrame))

(def defs     (filter #(.isFile %) (.listFiles (java.io.File. "defs/runs"))))
(def vertices (map #(.getName %) defs))
(def yaml     (org.yaml.snakeyaml.Yaml. (ExtendedConstructor.)))
(def extends  (map #(.get (.load yaml (slurp (.getPath %))) "ddts_extends") defs))
(def edges    (into {} (filter val (zipmap vertices extends))))
(def rootpath (memoize #(let [x (edges %)] (if x (conj {% x} (rootpath x)) {}))))

(defn graph [re]
  (let [filtered-edges (filter #(re-matches re (first %)) edges)
        complete-edges (reduce conj {} (map #(rootpath %) (keys filtered-edges)))]
    {:V (into #{} (concat (keys complete-edges) (vals complete-edges)))
     :E complete-edges}))

(defn -main [& args]
  (if (> (count args) 1)
    (println "Supply at most a single filtering prefix.")
    (let [G (graph (re-pattern (str (first args) ".*")))
          mxgraph (mxGraph.)
          parent (.getDefaultParent mxgraph)
          model (.getModel mxgraph)]
      (.beginUpdate model)
      (let [vs (:V G)
            mkv #(.insertVertex mxgraph parent nil % 20 20 80 30)
            vmap (apply hash-map (interleave vs (map mkv vs)))]
        (doseq [e (:E G)] (.insertEdge mxgraph parent nil "" (vmap (key e)) (vmap (val e)))))
      (.endUpdate model)
      (doto (JFrame. "defgraph")
        (.setSize 800 600)
        (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
        (.add (mxGraphComponent. mxgraph))
        (.setVisible true)))))
