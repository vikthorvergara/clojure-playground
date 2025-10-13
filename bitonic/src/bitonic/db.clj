(ns bitonic.db
  (:require [clojure.java.jdbc :as jdbc]))

(def db-spec
  {:dbtype "postgresql"
   :dbname "bitonicdb"
   :user "bitonic"
   :password "secret"
   :host "localhost"
   :port 5432})

(defn init-db! []
  (jdbc/db-do-commands db-spec
    (jdbc/create-table-ddl
      :test_results
      [[:id :serial "PRIMARY KEY"]
       [:test_name "TEXT"]
       [:status "TEXT"]
       [:expected "TEXT"]
       [:actual "TEXT"]
       [:ts "TIMESTAMP DEFAULT NOW()"]]
      {:conditional? true})))

(defn insert-result! [test-name status expected actual]
  (jdbc/insert! db-spec :test_results
                {:test_name test-name
                 :status status
                 :expected (pr-str expected)
                 :actual (pr-str actual)}))

(defn get-results []
  (jdbc/query db-spec ["SELECT * FROM test_results ORDER BY ts DESC"]))