(ns user
  "Namespace to make using the REPL more convenient."
  (:require
   [clojure.pprint :refer (pprint)]
   [clojure.repl :refer :all]
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.test :refer [run-tests run-all-tests]]
   [clojure.tools.namespace.repl :refer [refresh refresh-all]]
   [medley.core :as medley]))
