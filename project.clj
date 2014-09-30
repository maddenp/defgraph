(defproject confgraph "1"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.jgrapht/jgrapht-core "0.9.0"]
                 [org.yaml/snakeyaml "1.12"]]
  :java-source-paths ["src/java"]
  :main confgraph.core
  :source-paths ["src/clojure"]
  )
