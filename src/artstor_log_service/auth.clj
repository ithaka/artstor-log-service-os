(ns artstor-log-service.auth
  (require [org.ithaka.clj-iacauth.ring.middleware :as clj-iac]
           [clojure.tools.logging :as log]))

(defn with-user
  "A simple middleware wrapper to getting IAC user"
  ([handler] (with-user {} handler))
  ([{:keys [new-if-invalid exclude-paths] :or {new-if-invalid true exclude-paths []}} handler]
   (fn [req]
     (if (clj-iac/excluded? req exclude-paths)
       (handler req)
       (try
         (if (clj-iac/has-valid-web-token? req)
           (clj-iac/run-handler handler req (clj-iac/extract-user-from-token req))
           (if-let [[updated-triplet artstor-user-info] (clj-iac/extract-user-from-session req new-if-invalid)]
             (clj-iac/run-handler handler req artstor-user-info updated-triplet)
             (handler req)))
         (catch Exception e
           (log/error "Failed in clj-iacauth handler" e)
           {:status 503 :headers {clj-iac/error-header-name (.getMessage e)}
            :body "Service Unavailable -- We're experiencing some unexpected turbulence.  Please remain calm."}
           (handler req)))))))

(defn generate-web-token
  "Generate web token given profile id and ins id"
  ([user] (generate-web-token (user :profile-id) (user :institution-id) (user :default-user) (user :username)))
  ([profile-id ins-id] (generate-web-token profile-id ins-id true "default-user"))
  ([profile-id ins-id default-user user-name]
   (org.ithaka.clj-iacauth.token/generate {:profile-id profile-id :institution-id ins-id :default-user default-user :username user-name})))