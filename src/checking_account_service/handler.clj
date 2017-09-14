(ns checking_account_service.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [checking_account_service.models.operation :as Operation]))

(s/defschema Operation
  {(s/optional-key :id) Long
  (s/optional-key :account_number) Long
  :description s/Str
  :amount s/Num
  :date s/Str})

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Checking_account_service"
                    :description "Compojure Api example"}
             :tags [{:name "api", :description "some apis"}]}}}

    (context "/api/v1" []
      :tags ["api"]

      (POST "/operations" []
        :return Operation
        :body [operation Operation]
        :summary "add an operation to a given checking account"
        (ok (Operation/save! operation (:account_number operation)))))))
