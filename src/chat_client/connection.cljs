(ns chat-client.connection
  (:require [cognitect.transit :as transit]
            [reagent.core :as reagent :refer [cursor]]))

(defonce statuses [:connecting :open :closing :closed :init])

(defmulti handle-message
  (fn [_ msg] (get msg "$variant" :default)))

;TODO: maybe cursors are not best ideas here, check impl
(defmethod handle-message "Channels" [global-app-state msg]
  (let [chan-cur (cursor [:chat :channels] global-app-state)
        names (get msg "names")
        chan-tbl (zipmap names
                         (mapv #(hash-map :name %1) names))]
    (reset! chan-cur chan-tbl)))

(defmethod handle-message "JoinChannel" [global-app-state msg]
  (let [ch-name (get msg "name")
        ch-cur (cursor [:chat :channels ch-name] global-app-state)]
    (reset! ch-cur {:name ch-name
                    :users (vec (get msg "users"))
                    :messages (vec (get msg "messages" []))})))

(defmethod handle-message "Joined" [global-app-state msg]
  (let [ch-name (get msg "channel")
        new-user (get msg "user")
        channel-cur (cursor [:chat :channels ch-name] global-app-state)]
    (swap! channel-cur
           (fn [xs]
             (assoc xs
                    :users (vec (cons new-user (get xs :users [])))
                    :messages (vec (cons msg (get xs :messages []))))))))

(defmethod handle-message "Left" [global-app-state msg]
  (let [ch-name (get msg "channel")
        old-user (get msg "user")
        channel-cur (cursor [:chat :channels ch-name] global-app-state)]
    (swap! channel-cur
           (fn [xs]
             (assoc xs
                    :messages (vec (cons msg (get xs :messages [])))
                    :users (vec
                             (filter
                               #(= (get old-user "name") (get % "name"))
                               xs)))))))

(defmethod handle-message "Msg" [global-app-state msg]
  (let [ch-name (get msg "channel")
        channel-cur (cursor [:chat :channels ch-name] global-app-state)]
    (swap! channel-cur
           (fn [xs]
             (assoc xs :messages
                    (vec
                      (conj (:messages xs)
                            (get msg "message"))))))))

(defmethod handle-message :default [_ msg]
  (.error js/console "Got unknown message" (pr-str msg)))

(defn send! [socket data]
  (let [w (transit/writer :json-verbose)]
    (.send socket (transit/write w data))))

(defn close! [socket]
  (.close socket))

(defn create
  [global-app-state]
  (letfn [(on-error [ev]
            (let [status-cur (cursor [:connection :status] global-app-state)]
              (.error js/console "Connection error: " ev)
              (reset! status-cur :closed)))
          (on-open [ev]
            (let [ws (.-target ev)
                  status-cur (cursor [:connection :status] global-app-state)
                  user-login (get-in @global-app-state [:connection :username])
                  last-channel (get-in @global-app-state [:chat :active-channel])]
              (.log js/console "Opened connection: " ev)
              (reset! status-cur :open)
              ;make handshake and join with default chat
              (send! ws {"$variant" "Register"
                         "user" {"name" user-login}})
              (send! ws {"$variant" "JoinChannel"
                         "name" last-channel})))
          (on-message [ev]
            (let [r (transit/reader :json)
                  app-state global-app-state]
              (handle-message
                app-state
                (transit/read r (.-data ev)))))
          (on-close [ev]
            (let [status-cur (cursor [:connection :status] global-app-state)]
              (.debug js/console "Connection is closed." ev)
              (reset! status-cur :close)))]
    (let [url (get-in @global-app-state [:connection :url])
          ws (js/WebSocket. url)]
      (set! (.-onerror ws) on-error)
      (set! (.-onopen ws) on-open)
      (set! (.-onmessage ws) on-message)
      (set! (.-onclose ws) on-close)
      ws)))



