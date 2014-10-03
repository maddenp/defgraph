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
    {:v (into #{} (concat (keys complete-edges) (vals complete-edges)))
     :e complete-edges}))

(defn -main [& args]
  (if (> (count args) 1)
    (println "Supply at most a single filtering prefix.")
    (let [g (graph (re-pattern (str (first args) ".*")))
          mx (mxGraph.)
          root (.getDefaultParent mx)
          model (.getModel mx)]
      (doto mx
        (.setCellsDisconnectable false)
        (.setCellsEditable false)
        (.setCellsResizable false))
      (.beginUpdate model)
      (let [vs (:v g)
            fmkv #(.insertVertex mx root nil % 50 50 0 0)
            vmap (apply hash-map (interleave vs (map fmkv vs)))]
        (doseq [[label cell] vmap]
          (.setConnectable cell false)
          (.cellLabelChanged mx cell label true))
        (doseq [e (:e g)]
          (let [src (vmap (key e))
                dst (vmap (val e))] 
            (.setParent src dst)
            (let [new (.insertEdge mx root nil "" src dst)]
              (.setParent new src)))))
      (.endUpdate model)
      (doto (JFrame. "defgraph")
        (.setSize 800 600)
        (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
        (.add (mxGraphComponent. mx))
        (.setVisible true)))))
