(ns buran.core-test
  (:require
    [buran.core :refer :all]
    [clojure.test :refer :all]))


(def short-feed {:info   {:feed-type "atom_1.0"
                          :title     "Feed title"}
                 :entries [{:title       "Entry title"
                            :description {:value "entry description"}}]})


(deftest produce-feed
  (testing "Producing a feed"
    (is (= (produce {:feed         short-feed
                     :pretty-print false})
           "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<feed xmlns=\"http://www.w3.org/2005/Atom\"><title>Feed title</title><subtitle /><entry><title>Entry title</title><author><name /></author><summary>entry description</summary></entry></feed>\r\n"))))


(deftest produce-pretty-printed-feed
  (testing "Producing pretty printed feed"
    (is (= (produce short-feed)
           "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<feed xmlns=\"http://www.w3.org/2005/Atom\">\r\n  <title>Feed title</title>\r\n  <subtitle />\r\n  <entry>\r\n    <title>Entry title</title>\r\n    <author>\r\n      <name />\r\n    </author>\r\n    <summary>entry description</summary>\r\n  </entry>\r\n</feed>\r\n"))))
