(ns checking_account_service.core-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :refer :all]
            [checking_account_service.handler :refer :all]
            [checking_account_service.models.operation :as Operation]
            [checking_account_service.routes.accounts :as accounts]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [ring.mock.request :as mock]))

(def date-formatter (f/formatters :date))

(defn cleanup []
  (Operation/reset_storage!))

(defn wrap_date-time
  "Parses :date string attribute and assigns a date-time object to it."
  [operation]
  (assoc operation :date (f/parse date-formatter (:date operation))))

(defn create_operations
  "operations is a list of operation

  operation = {
    :account_number 1
    :description \"sample description\"
    :date \"\"
    :amount 12.0}]
    "
  [operations]
  (doseq [op operations]
    (let [account_number (:account_number op)
          operation (dissoc op :account_number)]
      (accounts/create-operation-handler account_number (wrap_date-time operation)))))

(defn setup_operations []
  (let [operations
    [
      {
        :account_number 1
        :description "sample description"
        :date "2017-09-14"
        :amount 20.0
      },
      {
        :account_number 1
        :description "sample description"
        :date "2017-09-14"
        :amount 120.0
      },
      {
        :account_number 2
        :description "sample description"
        :date "2017-09-14"
        :amount 20.0
      },
      {
        :account_number 2
        :description "sample description"
        :date "2017-09-14"
        :amount -120.0
      },
      {
        :account_number 3
        :description "sample description"
        :date "2017-09-14"
        :amount 20.0
      },
      {
        :account_number 3
        :description "sample description"
        :date "2018-09-14"
        :amount 12.5
      },
      {
        :account_number 4
        :description "sample description"
        :date "2018-09-01"
        :amount 20.0
      },
      {
        :account_number 4
        :description "sample description"
        :date "2018-09-02"
        :amount 100.0
      },
      {
        :account_number 4
        :description "sample description"
        :date "2018-09-02"
        :amount -100.0
      },
      {
        :account_number 4
        :description "sample description"
        :date "2018-09-15"
        :amount 1000.0
      },
      {
        :account_number 4
        :description "sample description"
        :date "2018-09-15"
        :amount -150.0
      },
      {
        :account_number 4
        :description "sample description"
        :date "2018-09-30"
        :amount 12.5
      }
    ]]
    (create_operations operations)))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(deftest balance_route
  (testing "GET request to /api/v1/accounts/:id/balance")
    (testing "with valid accounts"
      (setup_operations)
      (are [an b]
        (let [account_number an
              response (app (-> (mock/request :get  (str "/api/v1/accounts/" account_number "/balance"))
                                (mock/content-type "application/json")))
              body     (parse-body (:body response))
              balance b]
          (is (= 200 (:status response)))
          (is (= { :account_number account_number :balance balance } body)))
        ;account_number   balance
        1                 140.0
        2                 -100.0
        3                 32.5
      )
      (cleanup)
    )
    (testing "with invalid account numbers"
      (setup_operations)
      (are [an]
        (let [account_number an
              response (app (-> (mock/request :get  (str "/api/v1/accounts/" account_number "/balance"))
                                (mock/content-type "application/json")))
              body     (parse-body (:body response))]
          (is (= 400 (:status response)))
          (is (contains? (:errors body) :account_number)))
        ;account_number   balance
        100000000000 ; Account does not exist
        -1 ; Negative
        0 ; Zero
        "x" ; String
        1.1 ; Float
        ;[1] ; Vector
        ;'("r") ; List
        ;"" ; Empty string
      )
      (cleanup)))

(deftest operation_route

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
        ; Possible invalid date values
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

(deftest statements_route
  (testing "GET request to /api/v1/accounts/:id/statement"
    (testing "with invalid account number"
      (setup_operations)
      (are [account_number]
        (let [path (str "/api/v1/accounts/" account_number "/statement?start_date=2017-01-01&end_date=2017-01-30")
              response (app (-> (mock/request :get path)
                                (mock/content-type "application/json")))
              body     (parse-body (:body response))
              ]
          (is (= 400 (:status response)))
          (is (contains? (:errors body) :account_number))
          )
          ; Possible invalid account_number values
          -1  ; Negative number
          0   ; Zero
          1.1 ; Float
          "x" ; String
          ;""  ; Empty
      )
      (cleanup))

    (testing "with invalid period"
      (setup_operations)
      (are [start_date end_date start_error end_error]
        (let [path (str "/api/v1/accounts/4/statement?start_date=" start_date "&end_date=" end_date)
              response (app (-> (mock/request :get path)
                                (mock/content-type "application/json")))
              body     (parse-body (:body response))
              ]
          (is (= 400 (:status response)))
          (if start_error
            (is (contains? (:errors body) :start_date))
            true)
          (if end_error
            (is (contains? (:errors body) :end_date))
            true)
          )
          ; Possible invalid start_date and end_date values
          ; start_date  end_date      start_error end_error
          ""            "2017-09-01"  true        false     ; Empty string
          "A"           "2017-09-01"  true        false     ; String is not a date
          "2017-09-40"  "2017-09-01"  true        false     ; String is not a valid date
          "2017-13-01"  "2017-09-01"  true        false     ; String is not a valid date
          "2017-0901"   "2017-09-01"  true        false     ; String is not a well formed date (contains invalid month and no day)
          1.2           "2017-09-01"  true        false     ; Number
          "[a]"         "2017-09-01"  true        false     ; String Vector
          [1]           "2017-09-01"  true        false     ; Num Vector
          "(a)"         "2017-09-01"  true        false     ; String List
          '(1.1)        "2017-09-01"  true        false     ; Num List
      )
      (cleanup))

    (testing "with valid input data"
      (setup_operations)
      (are [account_number start_date end_date statement]
        (let [path (str "/api/v1/accounts/" account_number "/statement?start_date=" start_date "&end_date=" end_date)
              response (app (-> (mock/request :get path)
                                (mock/content-type "application/json")))
              body     (parse-body (:body response))
              ]
          (is (= 200 (:status response)))
          (is (= statement body))
          )

        ; Test case: Period includes all operations
        ;account_number   start_date    end_date
        4                 "2018-09-01"  "2018-09-30"
        ;expected statement
        {
          :account_number 4
          :start_date "2018-09-01"
          :end_date "2018-09-30"
          :day_statements [
            {
              :date "2018-09-01"
              :operations [
                {
                  :description "sample description"
                  :amount 20.0
                }
              ]
              :balance 20.0
            },
            {
              :date "2018-09-02"
              :operations [
                {
                  :description "sample description"
                  :amount 100.0
                },
                {
                  :description "sample description"
                  :amount -100.0
                }
              ]
              :balance 20.0
            },
            {
              :date "2018-09-15"
              :operations [
                {
                  :description "sample description"
                  :amount 1000.0
                },
                {
                  :description "sample description"
                  :amount -150.0
                }
              ]
              :balance 870.0
            },
            {
              :date "2018-09-30"
              :operations [
                {
                  :description "sample description"
                  :amount 12.5
                }
              ]
              :balance 882.5
            }
          ]
        }

        ; Test case: Period includes no operation
        ;account_number   start_date    end_date
        4                 "2017-09-01"  "2017-09-30"
        ;expected statement
        {
          :account_number 4
          :start_date "2017-09-01"
          :end_date "2017-09-30"
          :day_statements []
        }

        ; Test case: Period includes some operations
        ;account_number   start_date    end_date
        4                 "2017-09-01"  "2018-09-02"
        ;expected statement
        {
          :account_number 4
          :start_date "2017-09-01"
          :end_date "2018-09-02"
          :day_statements [
            {
              :date "2018-09-01"
              :operations [
                {
                  :description "sample description"
                  :amount 20.0
                }
              ]
              :balance 20.0
            },
            {
              :date "2018-09-02"
              :operations [
                {
                  :description "sample description"
                  :amount 100.0
                },
                {
                  :description "sample description"
                  :amount -100.0
                }
              ]
              :balance 20.0
            }
          ]
        }
      )
      (cleanup)
    )
  ))

(deftest debts_route
  )