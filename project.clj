(defproject buran "0.1.2"


  :description "Bidirectional, data-driven RSS/Atom feed consumer, producer and feeds aggregator"


  :url "https://github.com/alekseysotnikov/buran"


  :license {:name "Eclipse Public License 2.0"
            :url  "http://www.eclipse.org/legal/epl-v20.html"}


  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.rometools/rome "1.11.0"]]


  :deploy-repositories {"clojars-https" {:url "https://clojars.org/repo"
                                         :sign-releases false}}


  :plugins [[lein-cloverage "1.0.13"]]


  :profiles {:uberjar {:aot :all}
             :linters {:dependencies [[org.clojure/clojure "1.10.0"]
                                      [clj-kondo "2019.06.08-alpha-SNAPSHOT"]]
                       :plugins      [[lein-kibit "0.1.6"]
                                      [jonase/eastwood "0.3.5"]]}}


  :aliases {"deploy"   ["deploy" "clojars-https"]
            "kibit"    ["with-profile" "+linters" "kibit"]
            "eastwood" ["with-profile" "+linters" "eastwood" "{:continue-on-exception true :namespaces [:source-paths]}"]
            "kondo"    ["with-profile" "+linters" "run" "-m" "clj-kondo.main" "--lint" "src"]})
