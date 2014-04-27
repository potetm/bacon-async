(defproject bacon-async "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://exampl.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [org.clojure/core.async "0.1.298.0-2a82a1-alpha"]
                 [sablono "0.2.16"]]
  :plugins [[lein-cljsbuild "1.0.3"]
            [com.cemerick/clojurescript.test "0.3.0"]]
  :cljsbuild {:builds
              [{:id "clobaconjure"
                :source-paths ["src"]
                :compiler {:output-to "target/bacon-async.js"
                           ;; :preamble ["bacon-async/react.min.js"]
                           :optimizations :whitespace
                           :pretty-print true}}
               {:id "unit-test"
                :source-paths ["src" "test"]
                :compiler {:output-to "target/unit-test.js"
                           :preamble ["bacon-async/es5-shim.js" "bacon-async/react.js"]
                           :optimizations :whitespace
                           :pretty-print true}}
               {:id "react-test"
                :source-paths ["src"]
                :compiler {:output-to "examples/react-test.js"
                           :preamble ["bacon-async/react.js"]
                           :optimizations :whitespace
                           :pretty-print true}}]
              :test-commands {"unit-test" ["phantomjs" :runner
                                           "target/unit-test.js"]}})
