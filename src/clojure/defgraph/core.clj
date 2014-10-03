(ns defgraph.core
  (:gen-class)
  (:import com.mxgraph.swing.mxGraphComponent
           com.mxgraph.view.mxGraph
           javax.swing.JFrame
           com.mxgraph.layout.mxOrganicLayout))

(def defs     (filter #(.isFile %) (.listFiles (java.io.File. "defs/runs"))))
(def vertices (map #(.getName %) defs))
(def yaml     (org.yaml.snakeyaml.Yaml. (ExtendedConstructor.)))
(def extends  (map #(.get (.load yaml (slurp (.getPath %))) "ddts_extends") defs))
(def edges    (into {} (filter val (zipmap vertices extends))))
(def rootpath (memoize #(let [x (edges %)] (if x (conj {% x} (rootpath x)) {}))))

(defn graph [re]
  (let [filtered-edges (filter #(re-matches re (first %)) edges)
        complete-edges (reduce conj {} (map #(rootpath %) (keys filtered-edges)))]
    {:v (into #{} (concat (keys complete-edges) (vals complete-edges)))
     :e complete-edges}))

(defn -main [& args]
  (if (> (count args) 1)
    (println "Supply at most a single filtering prefix.")
    (let [g (graph (re-pattern (str (first args) ".*")))
          mx (mxGraph.)
          model (.getModel mx)
          root (.getDefaultParent mx)
          height 600
          width 800]
      (doto mx
        (.setCellsDisconnectable false)
        (.setCellsEditable false)
        (.setCellsResizable false))
      (do
        (.beginUpdate model)
        (let [vs (:v g)
              fmkv #(.insertVertex mx root nil % 10 10 0 0)
              vmap (apply hash-map (interleave vs (map fmkv vs)))]
          (doseq [[label cell] vmap]
            (.setConnectable cell false)
            (.cellLabelChanged mx cell label true))
          (doseq [e (:e g)]
            (.insertEdge mx root nil "" (vmap (key e)) (vmap (val e)))))
        (.endUpdate model))
      (do
        (.beginUpdate model)
        (let [layout (mxOrganicLayout. mx)]
          (doto layout
            (.execute root)))
        (.endUpdate model))
      (doto (JFrame. "defgraph")
        (.setSize width height)
        (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
        (.add (mxGraphComponent. mx))
        (.setVisible true)))))
