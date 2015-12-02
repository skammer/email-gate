(ns email-gate.email
  (:use clostache.parser)
  (:require
   [postal.core :refer :all]
   [clojure.walk :refer [keywordize-keys]]
   ))

(defn prepare-data [data]
  (keywordize-keys data))

(defn filter-file-fields [data]
  (->> data
       (filter #(:filename (second %)))
       (into {})))

(defn filter-nonfile-fields [data]
  (let [files (filter-file-fields data)
        non-file-keys (clojure.set/difference
                       (set (keys data))
                       (set (keys files)))]
    (select-keys data non-file-keys)))

(defn mustacheable-map [data]
  (map (fn [[k v]] {:key k :value v}) data))

(defn email-text
  ([data] (email-text data "template.mustache"))
  ([data path]
   {:type "text/html; charset=utf-8"
    :content (render-resource path data)}))

(defn subject [data]
  (render-resource "subject.mustache" data))

(defn attachment [{:keys [tempfile content-type filename]}]
  {:type :inline
   :content tempfile
   :content-type content-type
   :file-name filename})

(defn email-body [data]
  (conj (map attachment (vals (filter-file-fields data)))
        (email-text data)))

(defn send-email [email data settings]
  (let [{:keys [host user pass] :as config} (:mail-server settings)
        data (prepare-data data)]

    (future (send-message config
             {:from user
              :to email
              :subject (subject data)
              :body (email-body data)})
            (println "sent email to" email))))

(comment 
 (def tdata {"name" "asdasd" "idea" "asdasd" "file" {:filename "w8KwGHc.jpg" :content-type "image/jpeg" :tempfile (clojure.java.io/file "/var/folders/g4/mbh54gn57mjgf368gh6l7b880000gn/T/ring-multipart-3696005296900943554.tmp"), :size 209131} "email" "asdas@ad"})
  
 (def prepdata (prepare-data tdata))

 (println prepdata)

 (println (filter-file-fields prepdata))
 (println (filter-nonfile-fields prepdata))

 (println (render-resource "template.mustache" (filter-nonfile-fields prepdata)))
 (println (render "{{name}}" (filter-nonfile-fields prepdata)))
 )
