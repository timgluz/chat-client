(defproject chat-client "0.1.0-SNAPSHOT"
  :description "Simple chat client for Igeolise chat-server."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.cognitect/transit-cljs "0.8.192"]
                 [reagent "0.4.3"]
                 [reagent/reagent-cursor  "0.1.1"]
                 [com.cemerick/clojurescript.test "0.3.1"]]
  :plugins [[lein-cljsbuild "1.0.3"]
            [com.cemerick/clojurescript.test "0.3.1"]]
  :preamble ["resources/js/react-0.9.0.js"]
  :cljsbuild {
    :builds {:dev {:source-paths ["src"]
                   :compiler {:output-to "resources/js/chat-dev.js"
                              :optimizations :whitespace
                              :pretty-print true}}
             :prod {:source-paths ["src"]
                    :compiler {:output-to "resources/js/chat.js"
                               :optimizations :advanced
                               :pretty-print false}}
             :test {:source-paths ["src" "test"]
                    :compiler {:output-to "target/cljs/unit-tests.js"
                               :optimizations :whitespace
                               :pretty-print true}}}
    :test-commands {"unit" ["phantomjs" :runner
                            "resources/test/polyfill.js"
                            "resources/js/react-0.9.0.js"
                            "target/cljs/unit-tests.js"]}})
