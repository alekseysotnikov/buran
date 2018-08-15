(ns buran.core
  (:require [buran.data :refer [feed->clj clj->feed]])
  (:import [com.rometools.rome.io SyndFeedInput SyndFeedOutput XmlReader]
           [java.net URL]
           [java.util Locale]
           [java.io File Writer InputStream]))


(defn consume [{:keys [from validate locale xml-healer-on allow-doctypes]
                :as source
                :or   {validate       false
                       locale         (Locale/US)
                       xml-healer-on  true
                       allow-doctypes false}}]
  "
  from  - file path string, File, Reader, W3C DOM document, JDOM document, W3C SAX InputSource
  validate - indicates if the input should be validated.
  locale - java.util.Locale
  xml-healer-on - Healing trims leading chars from the stream (empty spaces and comments) until the XML prolog.
                  Healing resolves HTML entities (from literal to code number) in the reader.
                  The healing is done only with the File and Reader.
  allow-doctypes - you should only activate it when the feeds that you process are absolutely trustful
  "
  (let [consumer (doto
                   (SyndFeedInput. validate locale)
                   (.setAllowDoctypes allow-doctypes)
                   (.setXmlHealerOn xml-healer-on))
        from     (if (string? source) source from)
        from     (if (string? from) (File. from) from)]
    (feed->clj (.build consumer from))))


(defn- http-reader [{:keys [from headers lenient default-encoding]
                     :or   {default-encoding "US-ASCII"
                            lenient          true}}]
  "
  from - http url string, URL, File, InputStream
  headers - request's HTTP headers map
  lenient - indicates if the charset encoding detection should be relaxed. Default true
  default-encoding - default US-ASCII. Supported: UTF-8, UTF-16, UTF-16BE, UTF-16LE, CP1047, US-ASCII
  "
  (cond
    (instance? File from) (XmlReader. from)
    (instance? InputStream from) (XmlReader. from (:content-type headers) lenient default-encoding)
    :url (let [url  (if (instance? URL from) from (URL. from))
               from (.openConnection url)]
           (XmlReader. from headers))))


(defn consume-http [request]
  (let [request (if (string? request)
                  {:from request}
                  request)
        reader  (http-reader request)
        request (assoc request :from reader)]
    (consume request)))


(defn produce [{:keys [feed to pretty-print]
                :or   {to           :string
                       pretty-print true}}]
  (let [producer (SyndFeedOutput.)
        feed     (clj->feed feed)]
    (cond
      (= :string to) (.outputString producer feed pretty-print)
      (= :w3cdom to) (.outputW3CDom producer feed)
      (= :jdom to) (.outputJDom producer feed)
      (or (instance? File to) (.output producer feed to pretty-print)
          (instance? Writer to)) (.output producer feed to pretty-print))))


(defn combine-feeds [& feeds]
  "Combine entries of feeds into the first one feed"
  (assoc (first feeds) :entries (lazy-seq (apply concat (map :entries feeds)))))


(defn sort-entries-by
  ([keyfn feed]
   (sort-entries-by keyfn compare feed))
  ([keyfn comp feed]
   (update-in feed [:entries] #(sort-by keyfn comp %))))


(defn filter-entries [pred feed]
  (update-in feed [:entries] #(filter pred %)))