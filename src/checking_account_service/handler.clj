(ns checking_account_service.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [checking_account_service.routes.accounts :as accounts]))

(defroutes app-routes
  accounts/routes)

(def app
  (api
    {:swagger
     {:ui "/doc/v1/"
      :spec "/swagger.json"
      :data {:info {:title "Checking Account Service"
                    :description "A REST api for checking account service"}
             :tags [{:name "api", :description "REST api for checking account service"},
                    {:name "v1" , :description "Version 1 of api"}]}}}

    (GET "/" {{input :input} :params}
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body "<p>Checking account service is running.</p><a href='/doc/v1/'>See API</a><p>"})

    (context "/api/v1" []
      :tags ["api"]

    app-routes
)))
