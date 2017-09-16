(ns checking_account_service.models.operation
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
;            [clj-time.coerce :as coerce]
;            [cheshire.core :refer :all]
;            [cheshire.generate :as generate]
            ))

; (def date-formatter (f/formatters :date))

;(generate/add-encoder
;  org.joda.time.DateTime
;  (fn [data jsonGenerator]
;  (.writeString jsonGenerator (coerce/to-string data))))
; (.writeString jsonGenerator (coerce/to-string data))))

;(defn wrapp_operation_data [operation]
;  (assoc operation :date (f/parse date-formatter (:date operation))))

(def operations_storage (atom #{}))

(defn wrapp_operation_id [operation]
  (if (contains? operation :id)
    operation
    (assoc operation :id (inc (count @operations_storage)))))

(defn wrapp_account_number [operation account_number]
  (assoc operation :account_number account_number))

(defn filter_by_account
  "Filter the operations collection by account_number"
  [operations account_number]
  (filter (fn [operation] (= account_number (:account_number operation))) operations))

(defn filter_by_interval
  "Filter the operations collection by date interval"
  [operations start_date end_date]
  (let [test
        (fn [x]
          (t/within? (t/interval start_date end_date)
          x))]
    (filter (fn [op] (test (:date op))) operations)))

(defn reduce_to_balance
  "Calculates balance for operations collection"
  [operations]
  (reduce +
    (map :amount operations))
  )

(defn balance [account_number]
  (-> @operations_storage
    (filter_by_account account_number)
    (reduce_to_balance)
    ))

(defn statement [account_number start_date end_date]
  (-> @operations_storage
    (filter_by_account account_number)
    (filter_by_interval start_date end_date)
    ))

(defn save! [operation account_number]
  (let [wrapped_operation (wrapp_operation_id (wrapp_account_number operation account_number))]
    (swap! operations_storage conj wrapped_operation)
    wrapped_operation))
