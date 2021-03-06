(ns checking_account_service.routes.accounts
  (:require [compojure.api.sweet :refer [defroutes context GET POST describe]]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [cheshire.generate :as generate]
            [checking_account_service.models.operation :as Operation]
            [checking_account_service.models.account :as Account]
            [checking_account_service.models.statement :as Statement]
            [checking_account_service.models.debt :as Debt]))

; Add example and default values to swagger interface
; https://clojurians-log.clojureverse.org/ring-swagger/2016-10-06.html
; https://github.com/metosin/ring-swagger#example

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
  {:account_number AccountNumber
   :balance s/Num})

(s/defschema OperationStatement
  {:description s/Str
   :amount s/Num})

(s/defschema DayStatement
  {:date Date
   :operations [OperationStatement]
   :balance s/Num})

(s/defschema Statement
  {:account_number AccountNumber
   :start_date Date
   :end_date Date
   :day_statements [DayStatement]})

(s/defschema Debt
  {:principal s/Num
   :start Date
   (s/optional-key :end) Date})

(s/defschema Debts
  {:account_number AccountNumber
   :debts [Debt]})

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
      :path-params [account_number :- (describe AccountNumber "A positive integer" :default 1)]

      (POST "/operations" []
        :return (describe OperationOut "A JSON object." :example
                          {:id 3637
                           :account_number 1
                           :description "Purchase on Amazon"
                           :amount -100.2
                           :date (t/today)})
        :body [operation (describe OperationIn "Description must be provided (empty string is ok). Date must comply to format yyyy-mm-dd." :example
                                   {:description "Purchase on Amazon"
                                    :amount -100.2
                                    :date (t/today)})]
        :summary "Register an operation to a given checking account"
        :description "
Register a new operation for a given checking account.

Putting operations (deposits, salaries, credits) should have a **positive** amount value and taking operations
(purchases, withdrawals, debits) should have a **negative** amount value.
You can use any string as an valid operation description. No validation between the operation description and
the operation amount signal is performed. Your application is responsible for ensuring a putting operation has
a positive value and a taking operation has a negative value.

An operation with zero amount value is considered valid.

If an operation for a non-existent account is provided, an account with the provided account number is created
and the operation is registered for it. Account number must be a positive integer otherwise account creation
and operation registration fail.

Current date (today) is not used for validating the provided operation. You are allowed to insert operations
with any valida date including dates in the future.

Whenever an input error happens a message with the following format is returned:
{
  \"errors\": {
    \"**wrong_parameter**\": \"**Error message**\"
  }
}
"
        (create-operation-handler account_number operation))

      (GET "/balance" []
        :return (describe Balance "A JSON object." :example
                          {:account_number 1
                           :balance 153.3})
        :summary "Get the current balance for the given checking account"
        :description "
Calculates account balance considering all stored operations for _account_number_ account. Current date has no influence
in balance calculation. Even if the given account has operations that occur after the current date, all operations are
considered in balance calculation.

If there is no account with the provided account number, the following error is returned:
  {
    \"errors\": {
      \"account_number\": \"Account number is not valid\"
    }
  }
"
        (get-balance-handler account_number))

      (GET "/statement" []
        :return (describe Statement "A JSON object." :example
                          {:account_number 1
                           :start_date (t/today)
                           :end_date (t/plus (t/today) (t/days 7))
                           :day_statements [{:date (t/today)
                                             :operations [{:description "Purchase on Amazon"
                                                           :amount -100.2}]
                                             :balance -100.2}]})
        :query-params [start_date :- (describe Date "start date for period using yyyy-mm-dd format. Includes the date in the period.")
                       end_date :- (describe Date "end date for period using yyyy-mm-dd format. Includes the date in the period.")]
        :summary "Returns the bank statement of an account given a period of dates"
        :description "
Returns the bank statement for a given account and period. Check Statement schema in the Response Class section.

Statement is grouped by dates. For each date with one or more trasactions, date's balance and operations are presented.

If an invalid account_number is provided, a error is returned. If start_date is later than end_date, an empty statement is returned."
        (get-statement-handler account_number start_date end_date))

      (GET "/debts" []
        :return (describe Debts "A JSON object." :example
                          {:account_number 1
                           :debts [{:principal -27.35
                                    :start (t/today)
                                    :end (t/plus (t/today) (t/days 2))}]})
        :summary "Returns the periods which the account's balance was negative"
        :description "
Returns a sequence of debts periods and associated principal values for the given account number. Principal is always a negative value.
Please check Debts schema in the Response Class section. The return debts object may be composed by multiple debt periods.

Each debt period has a principal (negative) value, a start date and an date. If the given account has a negative balance, no end date is
provided for the last debt period."
        (get-debts-handler account_number)))))