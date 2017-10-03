(defproject defgraph "0.0.1"
  :aot [defgraph.core]
  :dependencies [[local/jgraphx "3.0.1.1"]
                 [org.clojure/clojure "1.8.0"]
                 [org.yaml/snakeyaml "1.18"]]
  :java-source-paths ["src/java"]
  :main defgraph.core
  :repositories {"local" "file:maven"}
  :source-paths ["src/clojure"]
  :uberjar-name "defgraph.jar")
