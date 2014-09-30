(defproject confgraph "1"
  :aot [confgraph.core]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.yaml/snakeyaml "1.12"]]
  :java-source-paths ["src/java"]
  :main confgraph.core
  :source-paths ["src/clojure"]
  )
