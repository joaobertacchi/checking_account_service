(ns checking_account_service.models.operation)

(def operations_storage (atom #{}))

(defn wrapp_operation_id [operation]
  (if (contains? operation :id)
    operation
    (assoc operation :id (inc (count @operations_storage)))))

(defn wrapp_account_number [operation account_number]
  (assoc operation :account_number account_number))

(defn filter_by_account [operations account_number]
  (filter (fn [operation] (= account_number (:account_number operation))) operations))

(defn balance [account_number]
  (reduce +
    (map :amount (filter_by_account @operations_storage account_number))))

(defn save! [operation account_number]
  (let [wrapped_operation (wrapp_operation_id (wrapp_account_number operation account_number))]
    (swap! operations_storage conj wrapped_operation)
    wrapped_operation))
