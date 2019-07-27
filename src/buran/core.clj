(ns buran.core
  (:require [buran.data :refer [feed->clj clj->feed]])
  (:import [com.rometools.rome.io SyndFeedInput SyndFeedOutput XmlReader]
           [java.net URL]
           [java.util Locale]
           [java.io File Writer InputStream StringReader]))


;; App API


(defmacro with-exception [throw? & body]
  (if throw?
    `(do ~@body)
    `(try
       ~@body
       (catch Throwable e#
         {:message (.getMessage e#)
          :error   e#}))))


(defn consume
  "
  source - either a map with options or a feed as String
  from  - String, File, Reader, W3C DOM document, JDOM document, W3C SAX InputSource
  validate - indicates if the input should be validated
  locale - java.util.Locale
  xml-healer-on - Healing trims leading chars from the stream (empty spaces and comments) until the XML prolog.
                  Healing resolves HTML entities (from literal to code number) in the reader.
                  The healing is done only with the File and Reader.
  allow-doctypes - you should only activate it when the feeds that you process are absolutely trustful
  "
  [{:keys [from validate locale xml-healer-on allow-doctypes throw-exception]
    :as   source
    :or   {validate        false
           locale          (Locale/US)
           xml-healer-on   true
           allow-doctypes  false
           throw-exception false}}]
  (with-exception throw-exception
                  (let [from     (if (string? source) (StringReader. source) from)
                        from     (if (string? from) (File. from) from)
                        consumer (doto
                                   (SyndFeedInput. validate locale)
                                   (.setAllowDoctypes allow-doctypes)
                                   (.setXmlHealerOn xml-healer-on))]
                    (feed->clj (.build consumer from)))))


(defn- http-reader
  "
  from - <http url string>, URL, File, InputStream
  headers - request's HTTP headers map
  lenient - indicates if the charset encoding detection should be relaxed
  default-encoding - UTF-8, UTF-16, UTF-16BE, UTF-16LE, CP1047, US-ASCII
  "
  [{:keys [from headers lenient default-encoding]
    :or   {default-encoding "US-ASCII"
           lenient          true}}]
  (condp instance? from
     String      (XmlReader. (.openConnection (URL. from)) headers)
     URL         (XmlReader. (.openConnection from) headers)
     File        (XmlReader. from)
     InputStream (XmlReader. from (:content-type headers) lenient default-encoding)))


(defn consume-http [request]
  (let [request (if (string? request)
                  {:from request}
                  request)
        reader  (http-reader request)
        request (assoc request :from reader)]
    (consume request)))


(defn produce
  "
  feed-as-arg - either a map with options or a feed to generate
  feed - a feed to generate
  to - <file path string>, :string, :w3cdom, :jdom, File, Writer
  pretty-print - pretty-print XML output
  "
  [{:keys [feed to pretty-print throw-exception]
    :as   feed-as-arg
    :or   {to              :string
           pretty-print    true
           throw-exception false}}]
  (with-exception throw-exception
                  (let [feed     (if (nil? feed) feed-as-arg feed)
                        feed     (clj->feed feed)
                        producer (SyndFeedOutput.)]
                    (cond
                      (= :string to) (.outputString producer feed pretty-print)
                      (= :w3cdom to) (.outputW3CDom producer feed)
                      (= :jdom to) (.outputJDom producer feed)
                      (string? to) (.output producer feed (File. to) pretty-print)
                      (or (instance? File to)
                          (instance? Writer to)) (.output producer feed to pretty-print)))))


;; Utilities


(defn combine-feeds
  "Combine entries of feeds, put into the first one feed and return it"
  [feed & feeds]
  (let [entries (mapcat :entries (cons feed feeds))]
    (assoc feed :entries (lazy-seq entries))))


(defn sort-entries-by
  ([keyfn feed]
   (sort-entries-by keyfn compare feed))
  ([keyfn comp feed]
   (update-in feed [:entries] #(sort-by keyfn comp %))))


(defn filter-entries [pred feed]
  (update-in feed [:entries] #(filter pred %)))


(defn shrink
  "Removes from `x` any empty sequential or nil values"
  [x]
  (cond
    (map? x) (not-empty
               (reduce-kv
                 (fn [s k v]
                   (let [v' (shrink v)]
                     (cond-> s
                             v' (assoc k v'))))
                 (empty x)
                 x))

    (string? x) (not-empty x)

    (sequential? x) (not-empty
                      (into
                        (empty x)
                        (->> x
                             (map shrink)
                             (filter identity))))

    :else x))