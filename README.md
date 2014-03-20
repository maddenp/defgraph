confgraph
=========

WORK IN PROGRESS

A tool to draw dependency graphs for [ddts](https://github.com/maddenp/ddts) configurations.

1. lein uberjar
2. cp target/confgraph-1-standalone.jar [$DDTSAPP](https://github.com/maddenp/ddts/blob/master/README)/confgraph.jar
3. cd [$DDTSAPP](https://github.com/maddenp/ddts/blob/master/README)
4. java -jar confgraph.jar [filter-prefix]
5. ls graph.svg

### License

The contents of this repository are released under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0) license.
