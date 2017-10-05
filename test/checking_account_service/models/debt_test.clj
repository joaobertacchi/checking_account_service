(ns checking_account_service.models.debt-test
    (:require [clojure.test :refer :all]
              [checking_account_service.models.debt :as Debt]))

(deftest debt
  (testing "set_end_date_to_debt_period"
    (let [debt_period {}
          date 1]
          (is (= {:end date} (Debt/set_end_date_to_debt_period debt_period date)))))
      
  (testing "set_end_date_in_last_debt_period"
    (let [debts [{} {}]
          date 1]
          (is (= [{} {:end date}] (Debt/set_end_date_in_last_debt_period debts date))))))