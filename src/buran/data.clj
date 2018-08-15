(ns buran.data
  (:import
    [com.rometools.rome.feed.synd
     SyndFeed SyndFeedImpl
     SyndEntry SyndEntryImpl
     SyndImage SyndImageImpl
     SyndCategory SyndCategoryImpl
     SyndPerson SyndPersonImpl
     SyndLink SyndLinkImpl
     SyndContent SyndContentImpl
     SyndEnclosure SyndEnclosureImpl]))


;; rome->clj transformers


(defn enclosure->clj [^SyndEnclosure o]
  {:url             (.getUrl o)
   :type            (.getType o)
   :length          (.getLength o)})


(defn content->clj [^SyndContent o]
  {:mode            (.getMode o)
   :type            (.getType o)
   :value           (.getValue o)})


(defn link->clj [^SyndLink o]
  {:hreflang        (.getHreflang o)
   :title           (.getTitle o)
   :href            (.getHref o)
   :type            (.getType o)
   :rel             (.getRel o)
   :length          (.getLength o)})


(defn person->clj [^SyndPerson o]
  {:name            (.getName o)
   :email           (.getEmail o)
   :uri             (.getUri o)})


(defn category->clj [^SyndCategory o]
  {:name            (.getName o)
   :taxonomy-uri    (.getTaxonomyUri o)})


(defn image->clj [^SyndImage o]
  {:title           (.getTitle o)
   :url             (.getUrl o)
   :width           (.getWidth o)
   :description     (.getDescription o)
   :height          (.getHeight o)
   :link            (.getLink o)})


(defn entry->clj [^SyndEntry o]
  {:updated-date    (.getUpdatedDate o)
   :title           (.getTitle o)
   :foreign-markup  (.getForeignMarkup o)
   :categories      (map category->clj (.getCategories o))
   :comments        (.getComments o)
   :contributors    (map person->clj (.getContributors o))
   :contents        (map content->clj (.getContents o))
   :published-date  (.getPublishedDate o)
   :uri             (.getUri o)
   :links           (map link->clj (.getLinks o))
   :author          (.getAuthor o)
   :description     (when-let [v (.getDescription o)] (content->clj v))
   :authors         (map person->clj (.getAuthors o))
   :link            (.getLink o)
   :enclosures      (map enclosure->clj (.getEnclosures o))})


(defn feed->clj [^SyndFeed o]
  {:info {:feed-type       (.getFeedType o)
          :web-master      (.getWebMaster o)
          :encoding        (.getEncoding o)
          :title           (.getTitle o)
          :categories      (map category->clj (.getCategories o))
          :generator       (.getGenerator o)
          :contributors    (map person->clj (.getContributors o))
          :published-date  (.getPublishedDate o)
          :copyright       (.getCopyright o)
          :image           (when-let [v (.getImage o)] (image->clj v))
          :uri             (.getUri o)
          :links           (map link->clj (.getLinks o))
          :author          (.getAuthor o)
          :description     (.getDescription o)
          :docs            (.getDocs o)
          :style-sheet     (.getStyleSheet o)
          :authors         (map person->clj (.getAuthors o))
          :language        (.getLanguage o)
          :icon            (when-let [v (.getIcon o)] (image->clj v))
          :link            (.getLink o)
          :managing-editor (.getManagingEditor o)}
   :entries         (map entry->clj (.getEntries o))
   :foreign-markup  (.getForeignMarkup o)})


;; clj->rome transformers


(defn ^SyndEnclosure clj->enclosure [m]
  (doto
    (SyndEnclosureImpl.)
    (.setUrl            (:url m))
    (.setType           (:type m))
    (.setLength         (:length m))))


(defn ^SyndContent clj->content [m]
  (doto
    (SyndContentImpl.)
    (.setMode           (:mode m))
    (.setType           (:type m))
    (.setValue          (:value m))))


(defn ^SyndLink clj->link [m]
  (doto
    (SyndLinkImpl.)
    (.setHreflang       (:hreflang m))
    (.setTitle          (:title m))
    (.setHref           (:href m))
    (.setType           (:type m))
    (.setRel            (:rel m))
    (.setLength         (:length m))))


(defn ^SyndPerson clj->person [m]
  (doto
    (SyndPersonImpl.)
    (.setName           (:name m))
    (.setEmail          (:email m))
    (.setUri            (:uri m))))


(defn ^SyndCategory clj->category [m]
  (doto
    (SyndCategoryImpl.)
    (.setName           (:name m))
    (.setTaxonomyUri    (:taxonomy-uri m))))


(defn ^SyndImage clj->image [m]
  (doto
    (SyndImageImpl.)
    (.setTitle          (:title m))
    (.setUrl            (:url m))
    (.setWidth          (:width m))
    (.setDescription    (:description m))
    (.setHeight         (:height m))
    (.setLink           (:link m))))


(defn ^SyndEntry clj->entry [m]
  (doto
    (SyndEntryImpl.)
    (.setUpdatedDate    (:updated-date m))
    (.setTitle          (:title m))
    (.setForeignMarkup  (:foreign-markup m))
    (.setCategories     (map clj->category (:categories m)))
    (.setComments       (:comments m))
    (.setContributors   (map clj->person (:contributors m)))
    (.setContents       (map clj->content (:contents m)))
    (.setPublishedDate  (:published-date m))
    (.setUri            (:uri m))
    (.setLinks          (map clj->link (:links m)))
    (.setAuthor         (:author m))
    (.setDescription    (when-let [r (:description m)] (clj->content r)))
    (.setAuthors        (map clj->person (:authors m)))
    (.setLink           (:link m))
    (.setEnclosures     (map clj->enclosure (:enclosures m)))))


(defn ^SyndFeed clj->feed [{:keys [info entries foreign-markup]}]
  (doto
    (SyndFeedImpl.)
    (.setFeedType       (:feed-type info))
    (.setWebMaster      (:web-master info))
    (.setEncoding       (:encoding info))
    (.setTitle          (:title info))
    (.setForeignMarkup  foreign-markup)
    (.setCategories     (map clj->category (:categories info)))
    (.setGenerator      (:generator info))
    (.setContributors   (map clj->person (:contributors info)))
    (.setPublishedDate  (:published-date info))
    (.setCopyright      (:copyright info))
    (.setEntries        (map clj->entry entries))
    (.setImage          (when-let [r (:image info)] (clj->image r)))
    (.setUri            (:uri info))
    (.setLinks          (map clj->link (:links info)))
    (.setAuthor         (:author info))
    (.setDescription    (:description info))
    (.setDocs           (:docs info))
    (.setStyleSheet     (:style-sheet info))
    (.setAuthors        (map clj->person (:authors info)))
    (.setLanguage       (:language info))
    (.setIcon           (when-let [r (:icon info)] (clj->image r)))
    (.setLink           (:link info))
    (.setManagingEditor (:managing-editor info))))