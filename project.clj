(defproject confgraph "1"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.yaml/snakeyaml "1.12"]
                 [lacij "0.8.1"]]
  :java-source-paths ["src/java"]
  :main confgraph.core
  :source-paths ["src/clojure"]
  )