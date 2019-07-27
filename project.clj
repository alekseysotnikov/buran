(defproject buran "0.1.3"


  :description "Bidirectional, data-driven RSS/Atom feed consumer, producer and feeds aggregator"


  :url "https://github.com/alekseysotnikov/buran"


  :license {:name "Apache License 2.0"
            :url  "https://www.apache.org/licenses/LICENSE-2.0.html"}


  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.rometools/rome "1.12.0"]]


  :plugins [[lein-cloverage "1.1.1"]]


  :aot :all


  :profiles {:uberjar {:aot :all}
             :linters {:dependencies [[org.clojure/clojure "1.10.0"]
                                      [clj-kondo "2019.07.18-alpha-SNAPSHOT"]]
                       :plugins      [[lein-kibit "0.1.7"]
                                      [jonase/eastwood "0.3.6"]]}}


  :deploy-repositories {"clojars" {:url "https://clojars.org/repo"
                                   :sign-releases false}}


  :aliases {"deploy"   ["deploy" "clojars"]
            "kibit"    ["with-profile" "+linters" "kibit"]
            "eastwood" ["with-profile" "+linters" "eastwood" "{:continue-on-exception true :namespaces [:source-paths]}"]
            "kondo"    ["with-profile" "+linters" "run" "-m" "clj-kondo.main" "--lint" "src"]})
