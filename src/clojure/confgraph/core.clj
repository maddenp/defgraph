(ns confgraph.core
  (:gen-class)
  (:import ExtendedConstructor)
  (:import java.io.File)
  (:import org.yaml.snakeyaml.Yaml))

(def confs    (for [x (.listFiles (File. "conf/runs")) :when (.isFile x)] x))
(def nodes    (for [x confs] (.getName x)))
(def yaml     (Yaml. (ExtendedConstructor.)))

(defn extends [conf]
  (.get (.load yaml (slurp (.getPath conf))) "extends"))

(def edges (zipmap nodes (for [x confs] (extends x))))

(def rootpath
  (memoize (fn [src]
             (let [dst (edges src)]
               (if (nil? dst)
                 {}
                 (conj {src dst} (rootpath dst)))))))

(defn filtered-edges [prefix]
  (let [re (re-pattern (str prefix ".*"))]
    (into {} (for [[src dst] edges :when (re-matches re src)] (rootpath src)))))

(defn edge-str [[src dst]] (str "  " src " -> " dst ";"))

(defn usage []
  (println "NEED USAGE STRING HERE")
  (System/exit 1))

(defn -main [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (let [nargs (count args)]
    (if (> nargs 1) (usage))
    (let [prefix (first args)]
      (println "digraph g {\n  overlap=scale;")
      (doseq [e (if (= nargs 0) edges (filtered-edges prefix))]
        (if (second e) (println (edge-str e))))
      (println "}"))))
