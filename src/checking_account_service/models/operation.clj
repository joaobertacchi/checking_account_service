(ns checking_account_service.models.operation)

(defn save! [operation]
    (:account_number operation))
