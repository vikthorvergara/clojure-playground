(ns bitonic.server
  (:require [ring.adapter.jetty :as jetty]
            [bitonic.api :as api]
            [bitonic.db :as db])
  (:gen-class))

;; Server configuration
(def config
  {:port 3000
   :join? false})

;; Server instance (atom to hold the running server)
(defonce server (atom nil))

(defn start-server
  "Start the HTTP server"
  ([] (start-server (:port config)))
  ([port]
   (when @server
     (println "Server already running. Stop it first."))
   (println "Initializing Redis connection...")
   (db/init-db!)
   (println "Starting server on port" port "...")
   (reset! server
           (jetty/run-jetty #'api/app
                           {:port port
                            :join? false}))
   (println "Server started successfully!")
   (println "API available at http://localhost:" port "/api")
   @server))

(defn stop-server
  "Stop the HTTP server"
  []
  (when @server
    (println "Stopping server...")
    (.stop @server)
    (reset! server nil)
    (println "Server stopped.")))

(defn restart-server
  "Restart the HTTP server"
  ([] (restart-server (:port config)))
  ([port]
   (stop-server)
   (Thread/sleep 500)
   (start-server port)))

(defn -main
  "Main entry point for running the server"
  [& args]
  (let [port (if (first args)
               (Integer/parseInt (first args))
               (:port config))]
    (start-server port)
    ;; Keep the main thread alive
    (.addShutdownHook (Runtime/getRuntime)
                     (Thread. stop-server))
    @(promise))) ; Block forever

;; REPL helpers
(comment
  ;; Start server in REPL
  (start-server)

  ;; Stop server
  (stop-server)

  ;; Restart server
  (restart-server)

  ;; Start on custom port
  (start-server 8080)
  )
