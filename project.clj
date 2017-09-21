 (defproject checking_account_service "0.1.0-SNAPSHOT"
   :description "FIXME: write description"
   :dependencies [[org.clojure/clojure "1.8.0"]
                  [metosin/compojure-api "1.1.11"]]
   :ring {:handler checking_account_service.handler/app}
   :uberjar-name "server.jar"
   :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                                  [cheshire "5.5.0"]
                                  [ring/ring-mock "0.3.0"]
                                  [clj-time "0.14.0"]
                                  [pjstadig/humane-test-output "0.8.3"]
                                  [com.jakemccrary/lein-test-refresh "0.21.1"]]
                   :plugins [[lein-ring "0.12.0"]]}})
