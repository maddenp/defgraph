confgraph
=========

WORK IN PROGRESS

A tool to visualize [DDTS](https://github.com/maddenp/ddts) definition-dependency graphs.

1. lein uberjar
2. cp target/confgraph-1-standalone.jar [$DDTSAPP](https://github.com/maddenp/ddts/blob/master/README)/confgraph.jar
3. cd [$DDTSAPP](https://github.com/maddenp/ddts/blob/master/README)
4. java -jar confgraph.jar [filter-prefix]
5. ls graph.svg
