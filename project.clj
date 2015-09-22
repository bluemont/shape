(defproject shape "0.1.0-SNAPSHOT"
  :description "Data-centric parsing in Clojure."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [[org.clojure/clojure "1.7.0"]
   [medley "0.7.0"]]
  :repl-options {:init-ns user}
  :profiles
  {:dev
   {:source-paths ["dev"]
    :dependencies
    [[org.clojure/tools.namespace "0.2.10"]]}})
