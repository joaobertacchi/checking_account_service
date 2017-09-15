(ns checking_account_service.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [checking_account_service.models.operation :as Operation]
            [checking_account_service.models.account :as Account]))

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

(s/defschema Balance
  {
    :account_number Long
    :balance s/Num
  })

(defn create-operation-handler [account_number operation]
  (if (Account/insert! account_number)
    (ok (Operation/save! operation account_number))
    (bad-request {:errors {:account_number "Account number is not valid"}})))

(defn get-balance-handler [account_number]
  (if (Account/is_valid? account_number)
      (ok {:account_number account_number
           :balance (Operation/balance account_number)})
      (bad-request {:errors {:account_number "Account number is not valid"}})))

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
            :return Balance
            :summary "Get the current balance for the given checking account"
            (get-balance-handler account_number))

          (POST "/operations" []
            :return OperationOut
            :body [operation OperationIn]
            :summary "Add an operation to a given checking account"
            (create-operation-handler account_number operation)))))))
