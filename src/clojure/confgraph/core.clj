(ns confgraph.core
  (:gen-class)
  (:import ExtendedConstructor)
  (:import java.io.File)
  (:import org.yaml.snakeyaml.Yaml))

(def yaml      (Yaml. (ExtendedConstructor.)))
(def extends   (memoize (fn [d] (.get (.load yaml (slurp (.getPath d))) "ddts_extends"))))
(def defs      (for [x (.listFiles (File. "defs/runs")) :when (.isFile x)] x))
(def vertices  (for [x defs] (.getName x)))
(def raw-edges (zipmap vertices (for [x defs] (extends x))))

(def rootpath
  (memoize (fn [src]
             (let [dst (raw-edges src)]
               (if (nil? dst)
                 {}
                 (conj {src dst} (rootpath dst)))))))

(defn edges [prefix]
  (let [re (re-pattern (str prefix ".*"))]
    (into {} (for [[src dst] raw-edges :when (re-matches re src)] (rootpath src)))))

(defn -main [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (if (> (count args) 1)
    (do (println "Supply at most a single filtering prefix.")
        (System/exit 1)))
  (let [fe (edges (first args))]
    (prn fe)))
