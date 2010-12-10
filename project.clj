(defproject fastcgi-ring "0.0.1-SNAPSHOT"
  :description "FastCGI middleware for Ring"
  :dependencies [[clojure "1.2.0"]
                 [ring/ring-core "0.3.5"]
                 [org.clojars.pepijndevos/jfastcgi "2.0"]]
  :dev-dependencies [[ring/ring-jetty-adapter "0.3.5"]])
