(ns email-gate.core
  (:gen-class)
  (:use ring.middleware.cors
        ring.middleware.multipart-params)
  (:require
   [org.httpkit.server :refer [run-server]]
   [email-gate.email :refer [send-email]]
   ))

(defn handle-info [req]
  {:status 200 :body "This is not a server you are looking for"})

(defn read-config [filename]
  (clojure.edn/read-string (slurp filename)))

(def settings (read-config "resources/config.edn"))

(defonce app-server (atom nil))

(defn forward-email [req]
  (let [data (:multipart-params req)]
    (pmap #(send-email % data settings) (:recepients settings))
    {:status 200 :body "ok"}))

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
  (reset! app-server (run-server application (:app-server settings))))
