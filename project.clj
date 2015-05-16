(defproject gmaps-cljs "0.1.0-SNAPSHOT"
  :description "CLJS wrapper for Google Maps APIv3"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :profiles {:dev {:plugins [[com.cemerick/austin "0.1.6"]]}}

  :plugins [[lein-cljsbuild "1.0.4"]
            [com.cemerick/clojurescript.test "0.3.3"]]

  :dependencies [[org.clojure/clojure "1.7.0-beta2"]
                 [org.clojure/clojurescript "0.0-2850"]

                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 [org.omcljs/om "0.8.8"]
                 [prismatic/om-tools "0.3.10"]

                 [com.cemerick/double-check "0.6.1"]]

  :source-paths ["src"]
  
  :cljsbuild {:test-commands {"test" ["xvfb-run" "-a" "slimerjs" :runner
                                      "target/test/test.js"]}
              :builds [{:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:pretty-print true
                                   :optimizations :whitespace
                                   :cache-analysis true
                                   :preamble ["react/react.js"]
                                   :externs
                                   ["bower_componenets/react-externs/externs.js"]
                                   :output-to "target/test/test.js"}}]}
  )
