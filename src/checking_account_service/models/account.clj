(ns checking_account_service.models.account)

(def accounts_storage (atom #{}))

(defn is_valid? [account_number]
    (contains? @accounts_storage account_number))

(defn insert! [account_number]
    (swap! accounts_storage conj account_number))