(defproject buran "0.1.1"


  :description "Bidirectional, data-driven RSS/Atom feed consumer, producer and feeds aggregator"


  :url "https://github.com/alekseysotnikov/buran"


  :license {:name "Eclipse Public License 2.0"
            :url  "http://www.eclipse.org/legal/epl-v20.html"}


  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.rometools/rome "1.11.0"]]



  :deploy-repositories {"clojars-https" {:url "https://clojars.org/repo"
                                         :sign-releases false}}


  :plugins [[lein-cloverage "1.0.13"]])
