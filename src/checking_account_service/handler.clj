(ns checking_account_service.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [checking_account_service.models.operation :as Operation]))

(s/defschema OperationIn
  {:description s/Str
  :amount s/Num
  :date s/Str})

(s/defschema OperationOut
  {:id Long
  :account_number Long
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

      (context "/accounts" []

        (context "/:account_number" []
          :path-params [account_number :- s/Int]

          (GET "/balance" []
            :return s/Num
            :summary "Get the current balance for the given checking account"
            (ok (Operation/balance account_number)))

          (POST "/operations" []
            :return OperationOut
            :body [operation OperationIn]
            :summary "add an operation to a given checking account"
            (ok (Operation/save! operation account_number))))))))
