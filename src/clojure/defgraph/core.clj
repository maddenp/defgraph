(ns defgraph.core
  (:gen-class)
  (:import [com.mxgraph.layout mxOrganicLayout]
           [com.mxgraph.swing mxGraphComponent]
           [com.mxgraph.util mxPoint]
           [com.mxgraph.view mxGraph]
           [java.awt BorderLayout]
           [java.awt.event MouseAdapter]
           [javax.swing JButton JFrame JPanel]
           [javax.swing.border EmptyBorder])
  (:require [clojure.java.io :as io]))

(def yaml (org.yaml.snakeyaml.Yaml. (ExtendedConstructor.)))

(def defs
  (memoize
   (fn
     [root]
     (filter #(.isFile %) (file-seq (io/file root))))))

(defn vertices
  [root]
  (map #(.getName %) (defs root)))

(defn extends
  [root]
  (map #(.get (.load yaml (slurp (.getPath %))) "ddts_extends") (defs root)))

(def edges
  (memoize
   (fn
     [root]
     (into {} (filter val (zipmap (vertices root) (extends root)))))))

(def rootpath
  (memoize
   (fn
     [root]
     (memoize #(let [x ((edges root) %)] (if x (conj {% x} ((rootpath root) x)) {}))))))

(defn graph
  [root re]
  (let [filtered-edges (filter #(re-matches re (first %)) (edges root))
        complete-edges (reduce conj {} (map #((rootpath root) %) (keys filtered-edges)))]
    {:v (into #{} (concat (keys complete-edges) (vals complete-edges)))
     :e complete-edges}))

(defn mx-layout
  [mx model root]
  (.beginUpdate model)
  (let [layout (mxOrganicLayout. mx)]
    (doto layout
      (.setBorderLineCostFactor 10)
      (.setEdgeLengthCostFactor 0.0005)
      (.setEdgeDistanceCostFactor 6000)
      (.setEdgeCrossingCostFactor 6000)
      (.execute root)))
  (.endUpdate model))

(defn mx-mkgc
  [g height width]
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

(defn mx-update
  [panel g height width button-panel]
  (doto panel
    (.removeAll)
    (.add button-panel BorderLayout/SOUTH)
    (.add (mx-mkgc g height width) BorderLayout/CENTER)))

(defn -main
  [& args]
  (if (nil? (first args))
    (println "Expected arguments: <defs-root-path> [filter-prefix]")
    (let [root (first args)
          prefix (second args)
          button (JButton. "layout")
          button-panel (JPanel.)
          g (graph root (re-pattern (str prefix ".*")))
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
