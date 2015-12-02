(defproject email-gate "0.1.0-SNAPSHOT"
  :description "Mail forwarding thing"
  :url "http://staging.cat"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [http-kit "2.1.18"]
                 [ring-cors "0.1.0"]
                 [ring "1.3.2"]
                 [ring/ring-core "1.4.0"]
                 [com.draines/postal "1.11.3"]
                 [de.ubercode.clostache/clostache "1.4.0"]])
