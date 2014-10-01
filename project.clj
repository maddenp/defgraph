(defproject defgraph "1"
  :aot [defgraph.core]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [jgraph/jgraph "5.13.0.0"]
                 [org.jgrapht/jgrapht-jdk1.5 "0.7.3"]
                 [org.yaml/snakeyaml "1.12"]]
  :java-source-paths ["src/java"]
  :main defgraph.core
  :source-paths ["src/clojure"]
  )
