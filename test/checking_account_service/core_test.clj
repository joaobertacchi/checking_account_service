(ns checking_account_service.core-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :refer :all]
            [checking_account_service.handler :refer :all]
            [checking_account_service.models.operation :as Operation]
            [ring.mock.request :as mock]))

(defn cleanup []
  (Operation/reset_storage!))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(deftest a-test

  (testing "POST request to /api/v1/accounts/:id/operation"

    (testing "with valid params"
      (let [operation {:description "Purchase on Amazon"
                      :amount 3.34
                      :date "2017-10-16"}
            response (app (-> (mock/request :post  "/api/v1/accounts/10000/operations")
                                (mock/content-type "application/json")
                                (mock/body  (cheshire/generate-string operation))))
            body     (parse-body (:body response))]
        (is (= (:status response) 200))
        (is (= body (assoc operation :id 1 :account_number 10000)))
      (cleanup)))

    (testing "with invalid account_number"
      (are [account_number]
        (let [operation {:description "Purchase on Amazon"
                        :amount 3.34
                        :date "2017-10-16"}
              response (app (-> (mock/request :post  (str "/api/v1/accounts/" account_number "/operations"))
                                (mock/content-type "application/json")
                                (mock/body  (cheshire/generate-string operation))))
              body     (parse-body (:body response))]
          (is (= (:status response) 400))
          ;(is (= body (assoc operation :id 1 :account_number 10000)))
        )
        ; Possible invalid account_number values
        -1  ; Negative number
        0   ; Zero
        1.1 ; Float
        "x" ; String
        ""  ; Empty
        ))
  ))
