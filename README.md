![](pic/buran.png)
> Buran (meaning "Snowstorm" or "Blizzard") was the first spaceplane to be produced as part of the Soviet/Russian Buran programme. [Wikipedia](https://en.wikipedia.org/wiki/Buran_(spacecraft))

# Buran

[![Clojars Project](https://img.shields.io/clojars/v/buran.svg)](https://clojars.org/buran)
[![CircleCI](https://circleci.com/gh/alekseysotnikov/buran.svg?style=shield)](https://circleci.com/gh/alekseysotnikov/buran)
[![codecov](https://codecov.io/gh/alekseysotnikov/buran/branch/master/graph/badge.svg)](https://codecov.io/gh/alekseysotnikov/buran)

Buran is a library designed to consume and produce any RSS/Atom feeds by using data-driven approach.
It works as [ROME](https://rometools.github.io/rome/) wrapper but in Buran, feeds are just data structures. 

Buran could be used as an aggregator of vary feed formats into regular Clojure data structures. If you consume a feed, Buran creates a map. Thus all you have to do is either read or manipulate the map as you wish using regular functions like ```filter```, ```sort```, ```assoc```, ```dissoc``` and so on. 
After the modifications, Buran can generate from it your own feed, for example in a different format (RSS 2.0, 1.0, 0.9x or Atom 1.0, 0.3).

### Installation

1. Add to *project.clj* - ```[buran "0.1.4"]```

2. Import 

in your namespace
```clojure
(:require [buran.core :refer [consume consume-http produce combine-feeds filter-entries sort-entries-by shrink]])
```
or REPL
````clojure
(require '[buran.core :refer [consume consume-http produce combine-feeds filter-entries sort-entries-by shrink]])
````

## Usage

No matter with which format of a feed you work, no matter you want to consume a feed or produce a new one. 
Every time you work with the same data structure.
Buran's API is short - `consume`, `consume-http`, `produce` and some helpers to manipulate the feeds `combine-feeds`, `filter-entries`, `sort-entries-by` and `shrink`. 
The basic workflow is continually passing a data structure to the API functions, see [Various options](#various-options) for details.

### examples

Consume a feed from String
````clojure
(def feed "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
           <feed xmlns=\"http://www.w3.org/2005/Atom\">
             <title>Feed title</title>
             <subtitle />
             <entry>
               <title>Entry title</title>
               <author>
                 <name />
               </author>
               <summary>entry description</summary>
             </entry>
           </feed>
           ")
(shrink (consume feed))
=>
{:info    {:feed-type "atom_1.0", 
           :title     "Feed title"},
 :entries [{:title       "Entry title", 
            :description {:value "entry description"}}]}
````

Produce a feed

````clojure
(def feed {:info {:feed-type "atom_1.0"
                  :title     "Feed title"}
           :entries [{:title       "Entry title"
                      :description {:value "entry description"}}]})
(produce feed)
=>
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
 <feed xmlns=\"http://www.w3.org/2005/Atom\">\r
   <title>Feed title</title>\r
   <subtitle />\r
   <entry>\r
     <title>Entry title</title>\r
     <author>\r
       <name />\r
     </author>\r
     <summary>entry description</summary>\r
   </entry>\r
 </feed>
 "
````

Consume a feed over http

````clojure
(consume-http "https://stackoverflow.com/feeds/tag?tagnames=clojure")
=>
{:info {...},
 :entries [...],
 :foreign-markup [...]}
````

Shrink a feed (remove nils, empty colls, maps and etc.)

```clojure
(shrink (consume-http "https://stackoverflow.com/feeds/tag?tagnames=clojure"))
=>
{:info {:description "most recent 30 from stackoverflow.com",
        :feed-type "atom_1.0",
        :published-date #inst"2018-08-20T08:03:33.000-00:00",
        :title "Active questions tagged clojure - Stack Overflow",
        :link "https://stackoverflow.com/questions/tagged/?tagnames=clojure&sort=active",
        :uri "https://stackoverflow.com/feeds/tag?tagnames=clojure",
        :links [{:href "https://stackoverflow.com/questions/tagged/?tagnames=clojure&sort=active",
                 :type "text/html",
                 :rel "alternate",
                 :length 0}, ...]},
 :entries [{:description {:type "html", :value "<p>..."},
            :updated-date #inst"2018-08-20T06:16:12.000-00:00",
            :foreign-markup [...],
            :published-date #inst"2018-08-20T05:54:39.000-00:00",
            :title "Clojure evaluate lazy sequence",
            :author "Constantine",
            :categories [{:name "clojure", :taxonomy-uri "https://stackoverflow.com/tags"}, ...],
            :link "https://stackoverflow.com/questions/51924808/clojure-evaluate-lazy-sequence",
            :uri "https://stackoverflow.com/q/51924808",
            :authors [{:name "Constantine", :uri "https://stackoverflow.com/users/4201205"}],
            :links [{:href "https://stackoverflow.com/questions/51924808/clojure-evaluate-lazy-sequence",
                     :rel "alternate",
                     :length 0}]}, ...],
 :foreign-markup [...]}
```
### Various options

#### Consume feed
````clojure
(consume {:from             (java.io.File. "~/feed.xml") 
                                        ; String, File, Reader, W3C DOM document, JDOM document, W3C SAX InputSource
          :validate         false       ; Indicates if the input should be validated
          :locale           (Locale/US) ; java.util.Locale
          :xml-healer-on    true        ; Healing trims leading chars from the stream (empty spaces and comments) until the XML prolog.
                                        ; Healing resolves HTML entities (from literal to code number) in the reader.
                                        ; The healing is done only with the File and Reader.
          :allow-doctypes   false       ; You should only activate it when the feeds that you process are absolutely trustful
          :throw-exception  false       ; false - return map with an exception, throw an exception otherwise
         })
````
````clojure
(consume-http {:from             "https://stackoverflow.com/feeds/tag?tagnames=clojure" 
                                                      ; <http url string>, URL, File, InputStream
               :headers          {"X-Header" "Value"} ; Request's HTTP headers map
               :lenient          true                 ; Indicates if the charset encoding detection should be relaxed
               :default-encoding "US-ASCII"           ; Supports: UTF-8, UTF-16, UTF-16BE, UTF-16LE, CP1047, US-ASCII
               :throw-exception  false                ; false - return map with an exception, throw an exception otherwise
               ... + all options which are applied to a consume call
              })
````
*Beware!* ```consume-http``` from either http url string or URL is rudimentary and works only for simplest cases. For instance, it does not follow HTTP 302 redirects.
Please consider using a separate library like [clj-http](https://github.com/dakrone/clj-http) or [http-kit](http://www.http-kit.org/client.html) for fetching the feed.



#### Produce feed
````clojure
(produce {:feed            {:info {:feed-type "atom_1.0" ; Supports: atom_1.0, atom_0.3, rss_2.0, 
                                                         ; rss_1.0, rss_0.94, rss_0.93, rss_0.92, 
                                                         ; rss_0.91U (Userland), rss_0.91N (Netscape), 
                                                         ; rss_0.9
                                   :title "Feed title"}
                            :entries [{:title       "Entry 1 title"
                                       :description {:value "entry description"}}]
                            :foreign-markup nil}

          :to              :string ; <file path string>, :string, :w3cdom, :jdom, File, Writer
          :pretty-print    true    ; Pretty-print XML output
          :throw-exception false   ; false - return map with an exception, throw an exception otherwise
         })
````

## License

Copyright © 2018-2020 Aleksei Sotnikov

Distributed under the Apache License 2.0
