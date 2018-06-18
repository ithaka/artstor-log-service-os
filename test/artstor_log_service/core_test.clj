(ns artstor-log-service.core-test
  (:require [clojure.test :refer :all]
            [artstor-log-service.core :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as cheshire]
            [artstor-log-service.auth :as auth]))

(def web-token (auth/generate-web-token 299277 1000 false "qa@artstor.org"))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(defn- match? [expected-log actual-log]
  (= expected-log
     (select-keys actual-log
                  (keys expected-log))))

(defn included? [expected logs]
  (some (partial match? expected) logs))

(deftest web-test
  (let [app (build-webapp nil {} {})]
    (testing "Test POST request to /log returns expected response without cookies"
      (let [json { :eventType "artstor_item_view_no_cookies" :referring_requestid "ref-request-id-from-ui"
                  :item_id "string" :uri "/secure/metadata/AIC_123232" :status_code "OK"
                  :reason "string"
                  :additional_fields {:testField1 12345 :testField2 "asdfg123" :testField3 ["asdfg" 123]}}
            response (app (-> (mock/request :post "/api/v1/log")
                              (mock/content-type "application/json")
                              (mock/header "Fastly-client-Ip" "12.12.12.1")
                              (mock/header "User-Agent" "mock")
                              (mock/body (cheshire/generate-string json))))
            body   (parse-body (:body response))]
        (is (= (:status response) 200))
        (is (= "artstor_item_view_no_cookies" (body :eventtype)))
        (is ( = [:_lb0 :referer :institution_id :testField2 :item_id :ip_address :eventid :user_agent :referring_requestid :requestid :reason :sessionid :testField1 :profileid :dests :tstamp_usec :origin :query_string :status_code :uri :uuid :eventtype :testField3] (keys body)))
        (is (included? {:testField2 "asdfg123" :ip_address "12.12.12.1" :user_agent "mock" :referring_requestid "ref-request-id-from-ui" :testField1 12345 :dests ["captains-log"]
                        :status_code "OK" :uri "/secure/metadata/AIC_123232" :uuid nil :eventtype  "artstor_item_view_no_cookies" :testField3 ["asdfg" 123]} [body]))))
    (testing "Test POST request to /log returns expected response with cookies"
      (let [json { :eventType "artstor_item_view_cookies" :referring_requestid "ref-request-id-from-ui"
                   :item_id "string" :uri "/secure/metadata/AIC_123232" :status_code "OK"
                   :reason "string"
                   :additional_fields {:testField1 12345 :testField2 "asdfg123" :testField3 ["asdfg" 123]}}
            response (app (-> (mock/request :post "/api/v1/log")
                              (mock/content-type "application/json")
                              (mock/header "Fastly-client-Ip" "12.12.12.1")
                              (mock/header "User-Agent" "mock")
                              (mock/header "Cookie" "AccessSession=H4sIAAAAAAAAAK2Uy27bMBBF9_4Kw-sw5WP4mO6cJmmL7tp0VXQxHJKtitQ2LLlAEeTfq5cjx0KALgJIAjT3cnh0RfJhsVyuqrRavl2uovLJMAFptgAKCGyhEiFgNCkXu7rozDy6K2JBRBKG8u7YRFqtDfnoFEAOihwWl3RglWMsQQ3uX6Pb4vXa22vp8crpdxCUv5G3yl7doFW31pnBvR_dnhA0KVfABIhsg0UdsuJcciFfxt50aH529mZ_yH3hD90PDZTVBrw1En3AXqp2fWMTLl17-UuFbmySflebTit0Xw9t6nr7vNDw8_du4jVzU3flb8uHtjaFa2ybqB26t9WRaKWUlLK_j0oelVA8EmMQpBIJMN4JSikItkplZYJvwzqOaf7ucj_o46ZuqubQVNvN00xPYjUXeZ_TM9wT4CABYTQOKR7yeVr6jcbJMmHsPtPmR56UE4hB-lrn_SSf_rK-9rj8vuifQ7AvhdoFZ84j1edJYg7Sp8yiGB8FsLeCOGghWQUky1GSnCX5fr897OYZXlFd8aB1gBdzIIfnQM6fE7kSSfoWQUcEAR5YxCSVQMOoMkUTUng9IutnRLOMpNKO2TnRZqVbItuuNnBJtJuXMSbICc3rEcnZPjDnQCYTYHRRoFdJQMEWSGoWoHP7u3QxWObL__-AxlVVN9OZYOx0JtzTS8LhBYGaZt-vz_FDv9wNu-Duw_rTup9y8fgPn6-QBmkFAAA;
                                                     AccessSessionSignature=6111c6c23a6ec5f0b9de808241ea5c0f6cdd306cc1842dc226fadb3e5c885f16;
                                                     AccessSessionTimedSignature=8569ec17ae58fb5cbfe4186e3d3dbce026aab684a28924cf657d05b5e4bd5bcd")
                              (mock/body (cheshire/generate-string json))))
            body   (parse-body (:body response))]
        (is (= (:status response) 200))
        (is (= "artstor_item_view_cookies" (body :eventtype)))
        (is ( = [:_lb0 :referer :institution_id :testField2 :item_id :ip_address :eventid :user_agent :referring_requestid :requestid :reason :sessionid :testField1 :profileid :dests :tstamp_usec :origin :query_string :status_code :uri :uuid :eventtype :testField3] (keys body)))
        (is (included? {:testField2 "asdfg123" :ip_address "12.12.12.1" :user_agent "mock" :referring_requestid "ref-request-id-from-ui" :testField1 12345 :dests ["captains-log"]
                        :status_code "OK" :uri "/secure/metadata/AIC_123232" :uuid nil :eventtype  "artstor_item_view_cookies" :testField3 ["asdfg" 123]} [body]))))
    (testing "Test POST request to /log returns expected response with web-token"
      (let [json { :eventType "artstor_item_view_web_token" :referring_requestid "ref-request-id-from-ui"
                  :item_id "string" :uri "/secure/metadata/AIC_123232" :status_code "OK"
                  :reason "string"
                  :additional_fields {:testField1 12345 :testField2 "asdfg123" :testField3 ["asdfg" 123]}}
            response (app (-> (mock/request :post "/api/v1/log")
                              (mock/content-type "application/json")
                              (mock/header "Fastly-client-Ip" "12.12.12.1")
                              (mock/header "User-Agent" "mock")
                              (mock/header "web-token" web-token)
                              (mock/body (cheshire/generate-string json))))
            body   (parse-body (:body response))]
        (is (= (:status response) 200))
        (is (= "artstor_item_view_web_token" (body :eventtype)))
        (is ( = [:_lb0 :referer :institution_id :testField2 :item_id :ip_address :eventid :user_agent :referring_requestid :requestid :reason :sessionid :testField1 :profileid :dests :tstamp_usec :origin :query_string :status_code :uri :uuid :eventtype :testField3] (keys body)))
        (is (included? {:testField2 "asdfg123" :ip_address "12.12.12.1" :profileid "299277" :institution_id "1000" :user_agent "mock" :referring_requestid "ref-request-id-from-ui" :testField1 12345
                        :dests ["captains-log"] :status_code "OK" :uri "/secure/metadata/AIC_123232" :uuid nil :eventtype  "artstor_item_view_web_token" :testField3 ["asdfg" 123]} [body]))))))