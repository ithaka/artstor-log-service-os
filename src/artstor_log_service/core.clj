(ns artstor-log-service.core
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [schema.core :as s]
            [clj-app-container.protocol :as p]
            [captains-common.core :as captains]
            [artstor-log-service.auth :as auth]
            [org.ithaka.clj-iacauth.core :refer [get-session-id]]
            [clj-sequoia.logging :refer [captains-log!]]))

(s/defschema LogMessage
  {:eventType s/Str
   (s/optional-key :referring_requestid) s/Str
   (s/optional-key :item_id) s/Str
   (s/optional-key :uri) s/Str
   (s/optional-key :status_code) s/Str
   (s/optional-key :description) s/Str
   (s/optional-key :origin) s/Str
   (s/optional-key :reason) s/Str
   (s/optional-key :additional_fields) s/Any})

(defn build-webapp [ this container-routes container-swagger]
  (->>
    (api
     {:swagger
      {:ui "/"
       :spec "/swagger.json"
       :data {:info {:title "Artstor Logging Service"
                     :description "This service can log any UI events from the front end so they can be tracked via Captains log"}
              :tag [{:name "loggy" :description "Log a UI event"}]
              :paths container-swagger}}}
       (context "/api/v1/log" []
                :tags ["loggy"]
                (POST "/" request
                  :return String
                      :body [log-message LogMessage]
                      :summary "Logs the provided message in the Captains log"
                      :responses {200 {:description "Log successful"}
                                  400 {:description "Invalid log message.  Bad UI, Bad!"}
                                  500 {:description "He's dead Jim"}}
                      (let [additional-fields (log-message :additional_fields)
                            trimmed-log-message (dissoc log-message :additional_fields :eventType)
                            data (merge (captains/captains-log-data request (captains/extract-user-or-ip request)) trimmed-log-message additional-fields)]
                      (ok (captains-log! (log-message :eventType) data))))))
    auth/with-user
    wrap-multipart-params
    wrap-cookies
    wrap-params))

(defrecord App []
  p/AppBase
  (init [this _] this)
  (config [_])
  p/Webapp
  (createWebapp [this container-routes container-swagger]
    (build-webapp this container-routes container-swagger)))
