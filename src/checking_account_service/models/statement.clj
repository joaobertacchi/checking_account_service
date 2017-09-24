(ns checking_account_service.models.statement
    (:require [clj-time.core :as t]
              [clj-time.format :as f]
              [checking_account_service.models.operation :as Operation]))

(defn operation_statement [operation]
  (select-keys operation [:description :amount]))

(defn day_statement
  "Create and return a day statement using date and operations"
  [date operations]
  {
    :date date
    :operations (map operation_statement (sort-by #(:id %) operations)) ; By sorting by :id we respect insertion order
    :balance (Operation/reduce_to_balance operations)
  })

(defn day_statements
  "grouped_operations is a map returned by (group-by :date operations)
  Return a sequence of day_statement ordered by date"
  [grouped_operations]
  (if-not (empty? grouped_operations)
    (->>
      grouped_operations

      ; Create a day_statement for each day
      (map 
        (fn [group]
          (let [date (get group 0)
                operations (get group 1)]
            (day_statement date operations))))

      ; Sort day_statements by date
      (sort-by #(:date %))

      ; Sum up balances. Each day balance must add last day balance
      (reductions
        (fn ([day_statement1 day_statement2]
              (assoc day_statement2 :balance (+ (:balance day_statement1) (:balance day_statement2))))
            ([day_statement1]
              day_statement1)))
    )
    [] ; Return empty Vector if groupded_operations is empty
  )
)

(defn statement
  ([account_number start_date end_date] ; Return statement for the period start_date to end_date
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

  ([account_number] ; Return statement for all the stored operations
    (let [grouped_operations
          (group-by :date
            (->
              (Operation/all)
              (Operation/filter_by_account account_number)
              ))
          ds (day_statements grouped_operations)]
      {
        :account_number account_number
        :start_date (:date (first ds))
        :end_date (:date (last ds))
        :day_statements ds
      }))
)
