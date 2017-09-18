(ns checking_account_service.models.statement
    (:require [clj-time.core :as t]
              [clj-time.format :as f]
              [checking_account_service.models.operation :as Operation]))

(defn operation_statement [operation]
  (select-keys operation [:description :amount]))

(defn day_statement [date operations]
  (let [op_stmt (filter operation_statement operations)]
    {
      :date date
      :operations (map operation_statement op_stmt)
      :balance (Operation/reduce_to_balance operations)
    }
  ))

(defn day_statements [grouped_operations]
  (->>
    grouped_operations
    (map 
      (fn [group]
        (let [date (get group 0)
              operations (get group 1)]
          (day_statement date operations))))
    (sort-by #(:date %))
    (reductions
      (fn ([day_statement1 day_statement2]
            (assoc day_statement2 :balance (+ (:balance day_statement1) (:balance day_statement2))))
          ([day_statement1]
            day_statement1)))
    ))

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
