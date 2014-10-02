(defproject defgraph "1"
  :aot [defgraph.core]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.yaml/snakeyaml "1.12"]]
  :java-source-paths ["src/java"]
  :main defgraph.core
  :source-paths ["src/clojure"]
  )
