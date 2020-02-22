(ns buran.core-test
  (:require
    [buran.core :refer :all]
    [clojure.test :refer :all])
  (:import [java.io ByteArrayInputStream File]))


(def short-feed {:info   {:feed-type "atom_1.0"
                          :title     "Feed title"}
                 :entries [{:title       "Entry title"
                            :description {:value "entry description"}}]})


(def short-feed-str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<feed xmlns=\"http://www.w3.org/2005/Atom\">\r\n  <title>Feed title</title>\r\n  <subtitle />\r\n  <entry>\r\n    <title>Entry title</title>\r\n    <author>\r\n      <name />\r\n    </author>\r\n    <summary>entry description</summary>\r\n  </entry>\r\n</feed>\r\n")


(deftest produce-feed
  (let [expected "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<feed xmlns=\"http://www.w3.org/2005/Atom\"><title>Feed title</title><subtitle /><entry><title>Entry title</title><author><name /></author><summary>entry description</summary></entry></feed>\r\n"]
    (testing "Produce the feed to various types"
      (is (= expected (produce {:feed         short-feed
                                :pretty-print false})))
      (is (instance? org.w3c.dom.Document (produce {:feed short-feed
                                                    :to   :w3cdom})))
      (is (instance? org.jdom2.Document (produce {:feed short-feed
                                                  :to   :jdom}))))
    (testing "Produce the feed to a file"
      (let [pathname "target/feed.xml"]
        (produce {:feed         short-feed
                  :to           pathname
                  :pretty-print false})
        (is (= expected (slurp pathname))))
      (let [pathname "target/feed2.xml"]
        (produce {:feed         short-feed
                  :to           (File. pathname)
                  :pretty-print false})
        (is (= expected (slurp pathname)))))))


(deftest produce-pretty-printed-feed
  (testing "Producing pretty printed feed"
    (is (= (produce short-feed)
           "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<feed xmlns=\"http://www.w3.org/2005/Atom\">\r\n  <title>Feed title</title>\r\n  <subtitle />\r\n  <entry>\r\n    <title>Entry title</title>\r\n    <author>\r\n      <name />\r\n    </author>\r\n    <summary>entry description</summary>\r\n  </entry>\r\n</feed>\r\n"))))


(deftest shrink-feed
  (is (= {:a {:aa 1}
          :b {:ba -1
              :bb 2}
          :k "abc"}
         (shrink {:a {:aa 1}
                  :b {:ba -1
                      :bb 2
                      :bc nil
                      :bd ""
                      :be []
                      :bf {}
                      :bg {:ga nil}
                      :bh [nil]
                      :bi [{}]
                      :bj [{:ja nil}]}
                  :c nil
                  :d ""
                  :e []
                  :f {}
                  :g {:ga nil}
                  :h [nil]
                  :i [{}]
                  :j [{:ja nil}]
                  :k "abc"
                  :l [{:a []}]}))))


(deftest combine-feeds-test
  (is (= {:info    {:feed-type "atom_1.0"
                    :title     "Feed title"}
          :entries '({:description {:value "entry description"}
                      :title       "Entry title"}
                     {:description {:value "entry description 2"}
                      :title       "Entry title 2"})}
         (combine-feeds {:info   {:feed-type "atom_1.0"
                                  :title     "Feed title"}
                         :entries [{:title       "Entry title"
                                    :description {:value "entry description"}}]}
                        {:info   {:feed-type "atom_1.0"
                                  :title     "Feed title 2"}
                         :entries [{:title       "Entry title 2"
                                    :description {:value "entry description 2"}}]}))))


(deftest consume-from-string
  (is (= short-feed (shrink (consume short-feed-str))))
  (is (= short-feed (shrink (consume {:from short-feed-str})))))


(deftest consume-http-test
  (testing "from input stream"
    (is (= short-feed (shrink (consume-http {:from (ByteArrayInputStream. (.getBytes short-feed-str))}))))))


(deftest negative-consume-http
  (let [{:keys [error message]} (consume-http "invalid://url")]
    (is (instance? Throwable error))
    (is (= "unknown protocol: invalid" message))))


(deftest negative-consume
  (testing "throwable"
    (is (instance? Throwable (try
                               (consume {:throw-exception true
                                         :from            "<invalid rss>"})
                               (catch Throwable e
                                 e)))))
  (testing "map with an exception"
    (let [{:keys [error message]} (consume {:throw-exception false
                                            :from            "<invalid rss>"})]
      (is (instance? Throwable error))
      (is (string? message)))))


(deftest consume-produce-roundtrip
  (is (= short-feed (-> short-feed
                        produce
                        consume
                        shrink)))
  (is (= short-feed-str (-> short-feed-str
                            consume
                            shrink
                            produce))))


(def real-feed '{:info    {:description    "most recent 30 from stackoverflow.com"
                           :feed-type      "atom_1.0"
                           :link           "https://stackoverflow.com/questions/tagged/?tagnames=clojure&sort=active"
                           :links          ({:href   "https://stackoverflow.com/feeds/tag?tagnames=clojure"
                                             :length 0
                                             :rel    "self"
                                             :type   "application/atom+xml"}
                                            {:href   "https://stackoverflow.com/questions/tagged/?tagnames=clojure&sort=active"
                                             :length 0
                                             :rel    "alternate"
                                             :type   "text/html"})
                           :published-date #inst "2020-02-22T16:12:21.000-00:00"
                           :title          "Active questions tagged clojure - Stack Overflow"
                           :uri            "https://stackoverflow.com/feeds/tag?tagnames=clojure"}
                 :entries [{:author         "Jim"
                            :authors        ({:name "Jim"
                                              :uri  "https://stackoverflow.com/users/5673289"})
                            :categories     ({:name         "bidi"
                                              :taxonomy-uri "https://stackoverflow.com/tags"}
                                             {:name         "ring"
                                              :taxonomy-uri "https://stackoverflow.com/tags"}
                                             {:name         "clojure"
                                              :taxonomy-uri "https://stackoverflow.com/tags"}
                                             {:name         "server"
                                              :taxonomy-uri "https://stackoverflow.com/tags"}
                                             {:name         "web"
                                              :taxonomy-uri "https://stackoverflow.com/tags"})
                            :description    {:type  "html"
                                             :value "I am learning Clojure for the web..."}
                            :link           "https://stackoverflow.com/questions/60234899/simple-web-app-of-bidi-ring-and-lein-gives-a-500-error"
                            :links          ({:href   "https://stackoverflow.com/questions/60234899/simple-web-app-of-bidi-ring-and-lein-gives-a-500-error"
                                              :length 0
                                              :rel    "alternate"})
                            :published-date #inst "2020-02-15T00:05:23.000-00:00"
                            :title          "Simple web app of Bidi, Ring and Lein gives a 500 error"
                            :updated-date   #inst "2020-02-15T00:47:32.000-00:00"
                            :uri            "https://stackoverflow.com/q/60234899"}
                           {:author         "Bob"
                            :authors        ({:name "Bob"
                                              :uri  "https://stackoverflow.com/users/5440125"})
                            :categories     ({:name         "cider"
                                              :taxonomy-uri "https://stackoverflow.com/tags"}
                                             {:name         "clojure"
                                              :taxonomy-uri "https://stackoverflow.com/tags"})
                            :description    {:type  "html"
                                             :value "The default cider-test-report reporter is ..."}
                            :link           "https://stackoverflow.com/questions/60235197/how-to-use-the-eftest-library-with-cider-test-report"
                            :links          ({:href   "https://stackoverflow.com/questions/60235197/how-to-use-the-eftest-library-with-cider-test-report"
                                              :length 0
                                              :rel    "alternate"})
                            :published-date #inst "2020-02-15T01:08:30.000-00:00"
                            :title          "How to use the eftest library with cider-test-report?"
                            :updated-date   #inst "2020-02-15T01:08:30.000-00:00"
                            :uri            "https://stackoverflow.com/q/60235197"}
                           {:author         "Bob"
                            :authors        ({:name "Bob"
                                              :uri  "https://stackoverflow.com/users/5440125"})
                            :categories     ({:name         "clojure"
                                              :taxonomy-uri "https://stackoverflow.com/tags"}
                                             {:name         "regex"
                                              :taxonomy-uri "https://stackoverflow.com/tags"})
                            :description    {:type  "html"
                                             :value "Suppose I want to unmap all the namespaces..."}
                            :link           "https://stackoverflow.com/questions/60243053/how-to-return-namespaces-by-regex-in-clojure"
                            :links          ({:href   "https://stackoverflow.com/questions/60243053/how-to-return-namespaces-by-regex-in-clojure"
                                              :length 0
                                              :rel    "alternate"})
                            :published-date #inst "2020-02-15T20:56:55.000-00:00"
                            :title          "How to return namespaces by regex in clojure?"
                            :updated-date   #inst "2020-02-15T23:34:26.000-00:00"
                            :uri            "https://stackoverflow.com/q/60243053"}]})


(deftest real-feed-roundtrip
  (is (= real-feed (-> real-feed
                       produce
                       consume
                       shrink
                       produce
                       consume
                       shrink))))


(deftest feed-utilities
  (is (= 2 (count (:entries (filter-entries #(= "Bob" (:author %)) real-feed)))))
  (is (= ["Bob" "Bob" "Jim"] (->> (sort-entries-by :author real-feed)
                                  :entries
                                  (map :author)))))