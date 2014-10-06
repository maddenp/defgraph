(ns defgraph.core
  (:gen-class)
  (:import com.mxgraph.layout.mxOrganicLayout
           com.mxgraph.util.mxPoint
           com.mxgraph.view.mxGraph
           javax.swing.JFrame
           com.mxgraph.swing.mxGraphComponent))

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

(defn layout-graph [mx model root]
  (.beginUpdate model)
  (let [layout (mxOrganicLayout. mx)]
    (doto layout
      (.setFineTuning false)
      (.setBorderLineCostFactor 10)
      (.setEdgeLengthCostFactor 0.001)
      (.setEdgeDistanceCostFactor 6000)
      (.setEdgeCrossingCostFactor 6000)
      (.setMaxIterations 1000)
      (.execute root)))
    (.endUpdate model))

(defn -main [& args]
  (if (> (count args) 1)
    (println "Supply at most a single filtering prefix.")
    (let [prefix (first args)
          g (graph (re-pattern (str prefix ".*")))
          mx (mxGraph.)
          model (.getModel mx)
          root (.getDefaultParent mx)
          height 1000
          width 1200]
      (doto mx
        (.setCellsDisconnectable false)
        (.setCellsEditable false)
        (.setCellsResizable false))
      (do
        (.beginUpdate model)
        (let [vs (:v g)
              fmkv #(.insertVertex mx root nil % (rand-int width) (rand-int height) 0 0)
              vmap (apply hash-map (interleave vs (map fmkv vs)))]
          (doseq [[label cell] vmap]
            (.setConnectable cell false)
            (.cellLabelChanged mx cell label true))
          (doseq [e (:e g)]
            (.insertEdge mx root nil "" (vmap (key e)) (vmap (val e)))))
        (.endUpdate model))
      (layout-graph mx model root)
      (let [gc (mxGraphComponent. mx)
            bounds (.getGraphBounds mx)
            view (.getView mx)]
        (let [offset-x (+ 20 (- (.getX bounds)))
              offset-y (+ 20 (- (.getY bounds)))]
          (.setTranslate view (mxPoint. offset-x offset-y)))
        (let [actual-height (.getHeight bounds)
              actual-width (.getWidth bounds)
              scale-factor (* 0.9 (min (/ width actual-width) (/ height actual-height)))]
          (.setScale view scale-factor))
        (doto (JFrame. (str "defgraph" (if prefix (str " (" prefix ")") "")))
          (.add gc)
          (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
          (.setSize width height)
          (.setVisible true))))))
