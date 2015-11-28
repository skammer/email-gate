(ns email-gate.core
  (:gen-class)
  (:use ring.middleware.cors
        ring.middleware.multipart-params)
  (:require
   [org.httpkit.server :refer [run-server]]
   [postal.core :refer :all]
   [clojure.string :refer [split-lines split]]
   [clojure.walk :refer [keywordize-keys]]))

(defn handle-info [req]
  {:status 200 :body "This is not a server you are looking for"})

(defn read-config [filename]
  (clojure.edn/read-string (slurp filename)))

(def settings (read-config "resources/config.edn"))

(defonce app-server (atom nil))

(defn send-email [target-email data]
  (let [{:keys [host user pass]} settings
        {:keys [name idea email file]} (keywordize-keys data)
        {:keys [tempfile content-type filename]} file]

    (send-message {:host host :user user :pass pass :ssl true}
                  {:from user
                   :to target-email
                   :subject (str "Заявка от " name)
                   :body [{:type "text/html; charset=utf-8"
                           :content (str "<p><b>Имя:</b> "   name "</p>"
                                         "<p><b>Идея:</b> "  idea "</p>"
                                         "<p><b>Почта:</b> " email "</p>") }
                          {:type :inline
                           :content tempfile
                           :content-type content-type
                           :file-name filename}]})
    (println "\nApplication sent form " email " to :" target-email)))

(defn forward-email [req]
  (let [data (:multipart-params req)]
    (map #(send-email % data) (:recepients settings))
    {:status 200 :body (:body req)}))

(defn handler [{method :request-method :as req}]
  (case method
    :get (handle-info req)
    :post (forward-email req)))

(def application
  (-> handler
      (wrap-multipart-params)
      (wrap-cors :access-control-allow-origin #".+")))

(defn stop-server []
  (when-not (nil? @app-server)
    (@app-server :timeout 100)
    (reset! app-server nil)))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating your server...")
  (reset! app-server (run-server application
                                 {:port (:port settings)})))

