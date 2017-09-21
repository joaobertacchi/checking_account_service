(ns checking_account_service.routes.accounts
    (:require [compojure.api.sweet :refer [defroutes context GET POST]]
              [ring.util.http-response :refer :all]
              [schema.core :as s]
              [clj-time.core :as t]
              [clj-time.format :as f]
              [cheshire.generate :as generate]
              [ring.swagger.json-schema :as rjs]
              [checking_account_service.models.operation :as Operation]
              [checking_account_service.models.account :as Account]
              [checking_account_service.models.statement :as Statement]
              [checking_account_service.models.debt :as Debt]))

(def date-formatter (f/formatters :date))

(generate/add-encoder org.joda.time.DateTime
                      (fn [data jsonGenerator]
                        (.writeString jsonGenerator (f/unparse date-formatter data))))


(defn positive?
  "Checks if num is positive"
  [num]
  (> num 0))

(defn full-date?
  "Checks if date follows full-date definition - RFC3339"
  [date]
  (t/equal? date (t/date-time (t/year date) (t/month date) (t/day date))))

(s/defschema Date
  (s/constrained org.joda.time.DateTime full-date?))

(s/defschema AccountNumber (s/constrained Long positive?))

(s/defschema OperationIn
  {:description s/Str
  :amount s/Num
  :date Date})

(s/defschema OperationOut
  {:id Long
  :account_number AccountNumber
  :description s/Str
  :amount s/Num
  :date Date})

(s/defschema Balance
  {
    :account_number AccountNumber
    :balance s/Num
  })

(s/defschema OperationStatement
  {:description s/Str
  :amount s/Num})

(s/defschema DayStatement
  {
    :date Date
    :operations [OperationStatement]
    :balance s/Num
  })

(s/defschema Statement
  {
    :account_number AccountNumber
    :start_date Date
    :end_date Date
    :day_statements [DayStatement]
  })

(s/defschema Debt
  {
    :principal s/Num
    :start Date
    :end (s/maybe Date)
  })

(s/defschema Debts
  {
    :account_number AccountNumber
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

(defroutes routes
      (context "/accounts" []

        (context "/:account_number" []
          :path-params [account_number :- (rjs/field AccountNumber {:default 1})]

          (GET "/balance" []
            :return (rjs/field Balance {:example
              {
                :account_number 1
                :balance 153.3
              }})
            :summary "Get the current balance for the given checking account"
            (get-balance-handler account_number))

          (POST "/operations" []
            :return (rjs/field OperationOut {:example
              {
                :id 3637
                :account_number 1
                :description "Purchase on Amazon"
                :amount 100.2
                :date (t/today)
              }})
            :body [operation (rjs/field OperationIn {:example
              {
                :description "Purchase on Amazon"
                :amount 100.2
                :date (t/today)
              }})]
            :summary "Add an operation to a given checking account"
            (create-operation-handler account_number operation))
            
          (GET "/statement" []
            :return (rjs/field Statement {:example
              {
                :account_number 1,
                :start_date (t/today)
                :end_date (t/plus (t/today) (t/days 7))
                :day_statements [
                  {
                    :date (t/today)
                    :operations [
                      {
                        :description "Purchase on Amazon"
                        :amount 100.2
                      }
                    ]
                    :balance 100.2
                  }
                ]
              }})
            :query-params [start_date :- Date, end_date :- Date]
            :summary "Returns the bank statement of an account given a period of dates"
            (get-statement-handler account_number start_date end_date))
            
          (GET "/debts" []
            :return (rjs/field Debts {:example
              {
                :account_number 1
                :debts [
                  {
                    :principal -27.35
                    :start (t/today)
                    :end (t/plus (t/today) (t/days 2))
                  }
                ]
              }})
            :summary "Returns the periods which the account's balance was negative"
            (get-debts-handler account_number))

          ))
  )