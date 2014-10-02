(defproject defgraph "1"
  :aot [defgraph.core]
  :dependencies [[local/jgraphx "3.0.1.1"]
                 [org.clojure/clojure "1.5.1"]
                 [org.yaml/snakeyaml "1.12"]]
  :java-source-paths ["src/java"]
  :main defgraph.core
  :repositories {"local" "file:maven"}
  :source-paths ["src/clojure"])
