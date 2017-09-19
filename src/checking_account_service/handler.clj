(ns checking_account_service.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [clj-time.core :as t]
            [checking_account_service.models.operation :as Operation]
            [checking_account_service.models.account :as Account]
            [checking_account_service.models.statement :as Statement]
            [checking_account_service.models.debt :as Debt]))

(s/defschema OperationIn
  {:description s/Str
  :amount s/Num
  :date org.joda.time.DateTime})

(s/defschema OperationOut
  {:id Long
  :account_number Long
  :description s/Str
  :amount s/Num
  :date org.joda.time.DateTime})

(s/defschema Balance
  {
    :account_number Long
    :balance s/Num
  })

(s/defschema OperationStatement
  {:description s/Str
  :amount s/Num})

(s/defschema DayStatement
  {
    :date org.joda.time.DateTime
    :operations [OperationStatement]
    :balance Long
  })

(s/defschema Statement
  {
    :account_number Long
    :start_date org.joda.time.DateTime
    :end_date org.joda.time.DateTime
    :day_statements [DayStatement]
  })

(s/defschema Debt
  {
    :principal s/Num
    :start org.joda.time.DateTime
    :end (s/maybe org.joda.time.DateTime)
  })

(s/defschema Debts
  {
    :account_number Long
    :debts [Debt]
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

(defn get-statement-handler [account_number start_date end_date]
  (if (Account/is_valid? account_number)
    (let [stmt (Statement/statement account_number start_date end_date)]
      (ok stmt))
    (bad-request {:errors {:account_number "Account number is not valid"}})))

(defn get-debts-handler [account_number]
  (if (Account/is_valid? account_number)
  (let [stmt (Statement/statement account_number)]
    (ok {:account_number account_number :debts (reduce Debt/reduce_day_to_debt_period [] (:day_statements stmt))}))
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
            (create-operation-handler account_number operation))
            
          (GET "/statement" []
            :return Statement
            :query-params [start_date :- org.joda.time.DateTime, end_date :- org.joda.time.DateTime]
            :summary "Returns the bank statement of an account given a period of dates"
            (get-statement-handler account_number start_date end_date))
            
          (GET "/debts" []
            :return Debts
            :summary "Returns the periods which the account's balance was negative"
            (get-debts-handler account_number))

          )))))
