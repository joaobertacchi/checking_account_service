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

(deftest operation_endpoint

  (testing "POST request to /api/v1/accounts/:id/operation"

    (testing "with valid params"
      (are [description amount date parsed_date]
        (let [operation {:description description
                        :amount amount
                        :date date}
              response (app (-> (mock/request :post  "/api/v1/accounts/10000/operations")
                                  (mock/content-type "application/json")
                                  (mock/body  (cheshire/generate-string operation))))
              body     (parse-body (:body response))]
          (println (str "Running for description=" description " amount=" amount " date=" date))
          (is (= 200 (:status response)))
          (is (= (assoc operation :id 1 :account_number 10000 :date parsed_date) body))
          (cleanup))
        ;description          amount  input_date    parsed_date
        "Purchase on Amazon"  3.34    "2017-10-1"   "2017-10-01"
        ""                    1       "2017-10-01"  "2017-10-01"
        "Purchase on Amazon"  -200    "2017-10-01"  "2017-10-01"
        "Purchase on Amazon"  -30.8   "2017-10-01"  "2017-10-01"
        "Purchase on Amazon"  -30.8   "2017-10"     "2017-10-01"
        "Purchase on Amazon"  -30.8   "20171001"    "20171001-01-01"; Date contains only year
        "Purchase on Amazon"  -30.8   "201710-01"   "201710-01-01"; Date contains year and month
      ))

    (testing "with invalid account_number"
      (are [account_number]
        (let [operation {:description "Purchase on Amazon"
                        :amount 3.34
                        :date "2017-10-16"}
              response (app (-> (mock/request :post  (str "/api/v1/accounts/" account_number "/operations"))
                                (mock/content-type "application/json")
                                (mock/body  (cheshire/generate-string operation))))
              body     (parse-body (:body response))]
          (is (= 400 (:status response)))
          (is (contains? (:errors body) :account_number))
          (cleanup)
        )
        ; Possible invalid account_number values
        -1  ; Negative number
        0   ; Zero
        1.1 ; Float
        "x" ; String
        ;""  ; Empty
        ))

    (testing "with invalid description"
      (are [description]
        (let [operation {:description description
                        :amount 3.34
                        :date "2017-10-16"}
              response (app (-> (mock/request :post  (str "/api/v1/accounts/1/operations"))
                                (mock/content-type "application/json")
                                (mock/body  (cheshire/generate-string operation))))
              body     (parse-body (:body response))]
          (is (= 400 (:status response)))
          (is (contains? (:errors body) :description))
          (cleanup)
        )
        ; Possible invalid description values
        1  ; Int
        1.1 ; Float
        ["a"] ; Vector
        '("a") ; List
        ))

    (testing "with no description"
      (let [operation {:amount 3.34
                      :date "2017-10-16"}
            response (app (-> (mock/request :post  (str "/api/v1/accounts/1/operations"))
                              (mock/content-type "application/json")
                              (mock/body  (cheshire/generate-string operation))))
            body     (parse-body (:body response))]
        (is (= 400 (:status response)))
        (is (contains? (:errors body) :description))
        (cleanup)
      ))

    (testing "with invalid amount"
      (are [amount]
        (let [operation {:description "Purchase on Amazon"
                        :amount amount
                        :date "2017-10-16"}
              response (app (-> (mock/request :post  (str "/api/v1/accounts/1/operations"))
                                (mock/content-type "application/json")
                                (mock/body  (cheshire/generate-string operation))))
              body     (parse-body (:body response))]
          (is (= 400 (:status response)))
          (is (contains? (:errors body) :amount))
          (cleanup)
        )
        ; Possible invalid description values
        ""  ; Empty string
        "A" ; String
        ["a"] ; String Vector
        [1] ; Num Vector
        '("a") ; String List
        '(1.1) ; Num List
        ))

    (testing "with invalid date"
      (are [date]
        (let [operation {:description "Purchase on Amazon"
                        :amount 300
                        :date date}
              response (app (-> (mock/request :post  (str "/api/v1/accounts/1/operations"))
                                (mock/content-type "application/json")
                                (mock/body  (cheshire/generate-string operation))))
              body     (parse-body (:body response))]
          (println (str "Running for date=" date))
          (is (= 400 (:status response)))
          (is (contains? (:errors body) :date))
          (cleanup)
        )
        ; Possible invalid description values
        ""  ; Empty string
        "A" ; String is not a date
        "2017-09-40" ; String is not a valid date
        "2017-13-01" ; String is not a valid date
        "2017-0901" ; String is not a well formed date (contains invalid month and no day)
        1.2 ; Number
        ["a"] ; String Vector
        [1] ; Num Vector
        '("a") ; String List
        '(1.1) ; Num List
        ))
  ))
