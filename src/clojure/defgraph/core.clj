(ns defgraph.core
  (:gen-class)
  (:import [com.mxgraph.layout mxOrganicLayout]
           [com.mxgraph.swing mxGraphComponent]
           [com.mxgraph.util mxPoint]
           [com.mxgraph.view mxGraph]
           [java.awt BorderLayout]
           [java.awt.event MouseAdapter]
           [javax.swing JButton JFrame JPanel]
           [javax.swing.border EmptyBorder]))

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

(defn mx-layout [mx model root]
  (.beginUpdate model)
  (let [layout (mxOrganicLayout. mx)]
    (doto layout
      (.setBorderLineCostFactor 10)
      (.setEdgeLengthCostFactor 0.0005)
      (.setEdgeDistanceCostFactor 6000)
      (.setEdgeCrossingCostFactor 6000)
      (.execute root)))
  (.endUpdate model))

(defn mx-mkgc [g height width]
  (let [mx (mxGraph.)
        model (.getModel mx)
        root (.getDefaultParent mx)]
    (doto mx
      (.setCellsDisconnectable false)
      (.setCellsEditable false)
      (.setCellsResizable false))
    (.beginUpdate model)
    (let [vs (:v g)
          fmkv #(.insertVertex mx root nil % (rand-int width) (rand-int height) 0 0)
          vmap (apply hash-map (interleave vs (map fmkv vs)))]
      (doseq [[label cell] vmap]
        (.setConnectable cell false)
        (.cellLabelChanged mx cell label true))
      (doseq [e (:e g)]
        (.insertEdge mx root nil "" (vmap (key e)) (vmap (val e)))))
    (.endUpdate model)
    (mx-layout mx model root)
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
      gc)))

(defn mx-update [panel g height width button-panel]
  (doto panel
    (.removeAll)
    (.add button-panel BorderLayout/SOUTH)
    (.add (mx-mkgc g height width) BorderLayout/CENTER)))
  
(defn -main [& args]
  (if (> (count args) 1)
    (println "Supply at most a single filtering prefix.")
    (let [prefix (first args)
          button (JButton. "layout")
          button-panel (JPanel.)
          g (graph (re-pattern (str prefix ".*")))
          panel (JPanel. (BorderLayout.))
          width 1200
          height 700
          frame (JFrame. (str "defgraph" (if prefix (str " (" prefix ")") "")))]
      (.add button-panel button)
      (mx-update panel g height width button-panel)
      (let [f #(do
                      (.setVisible frame false)
                      (doto panel
                        (.removeAll)
                        (mx-update g height width button-panel))
                      (.setVisible frame true))]
        (.addMouseListener button (proxy [MouseAdapter] [] (mousePressed [e] (f)))))
      (doto frame
        (.add panel)
        (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
        (.setSize width height)
        (.setVisible true)))))
