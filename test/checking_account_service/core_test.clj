(ns checking_account_service.core-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :refer :all]
            [checking_account_service.handler :refer :all]
            [ring.mock.request :as mock]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(deftest a-test

  (testing "Test POST request to /api/v1/operation returns the id of the created operation"
    (let [operation {:account_id 1
                    :description "Purchase on Amazon"
                    :amount 3.34
                    :date "2017-10-16"}
          response (app (-> (mock/request :post  "/api/v1/operations")
                            (mock/content-type "application/json")
                            (mock/body  (cheshire/generate-string operation))))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= body 1)))))
