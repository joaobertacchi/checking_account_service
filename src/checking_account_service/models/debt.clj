(ns checking_account_service.models.debt
    (:require [clj-time.core :as t]
              [clj-time.format :as f]))

(defn day_to_debt
  "Converts a day statement into a debt period"
  [day]
  {
    :principal (:balance day)
    :start (:date day)
    :end nil
  })

(defn set_end_date [debts date]
  (let [peek_debt_period (peek debts)]
    (conj (pop debts) (assoc peek_debt_period :end date))))

(defn reduce_day_to_debt_period
  "Reduce a day to a debt period"
  [debts day]
  ;(println debts)
  ;(println day)
  (let [peek_debt_period (peek debts)
        balance (:balance day)]
        (if-not peek_debt_period ; tests debts is empty
          (if (< balance 0) ; debts is empty: tests if day reduces to a new elem
            (conj debts (day_to_debt day)) ; debts is empty AND day reduces to an element
            debts) ; debts is empty AND day does not reduce to debts list
          (if (and (= balance (:principal peek_debt_period) (= nil (:end peek_debt_period)))) ; debts is NOT empty: test if balance equals last debt period
            debts ; day is not merged into last debt period
            (if-not (:end peek_debt_period) ; debts is NOT empty AND balance changed: test if peek_debt_period is open (has no end date)
              (if (>= balance 0) ; debts is NOT empty AND balance changed AND peek_debt_period open: 
                (set_end_date debts (t/minus (:date day) (t/days 1))) ; let's close it. Period!
                (conj (set_end_date debts (t/minus (:date day) (t/days 1))) ; let's close it and create a new period new debt principal
                  (day_to_debt day)) 
                )
              (if (>= balance 0) ; debts is NOT empty AND balance changed AND peek_debt_period is closed
                debts ; debts is NOT empty AND balance changed AND peek_debt_period is closed AND day balance is not negative (ignore day)
                (conj debts (day_to_debt day)); debts is NOT empty AND balance changed AND peek_debt_period is closed AND day balance negative (include new debt period)
              )
            )
          )
        )
    ))