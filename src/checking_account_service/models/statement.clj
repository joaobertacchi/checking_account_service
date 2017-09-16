(ns checking_account_service.models.statement
    (:require [clj-time.core :as t]
              [clj-time.format :as f]
              [checking_account_service.models.operation :as Operation]))

(defn operation_statement [operation]
  (select-keys operation [:description :amount]))

(defn day_statement [date operations]
  (let [op_stmt (filter operation_statement operations)]
    {
      :operations op_stmt
      :date (:date (get op_stmt 0))
      :balance (Operation/reduce_to_balance operations)
    }
  ))

(defn day_statements [grouped_operations]
  (map 
    (fn [group]
      (let [date (get group 0)
            operations (get group 1)]
        (day_statement key operations))) grouped_operations))
;    grouped_operations)
;    (for [key (sort (keys grouped_operations))]
;      (conj sts (day_statement key (get grouped_operations key))))
;    sts))

(defn statement [account_number start_date end_date]
  (let [grouped_operations
        (group-by :date
          (->
            (Operation/all)
            (Operation/filter_by_account account_number)
            (Operation/filter_by_interval start_date (t/plus end_date (t/days 1)))
            ))]
    {
      :account_number account_number
      :start_date start_date
      :end_date end_date
      :day_statements (day_statements grouped_operations)
    }))
