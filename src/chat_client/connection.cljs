(ns chat-client.connection
  (:require [cognitect.transit :as transit]
            [reagent.core :as reagent :refer [cursor]]))

(defonce statuses [:connecting :open :closing :closed :init])

(defmulti handle-message
  (fn [_ msg] (get msg "$variant" :default)))

(defmethod handle-message "Channels"
  [global-app-state msg]
  (let [channel-cur (cursor [:chat :channels] global-app-state)
        names (get msg "names")
        chan-tbl (zipmap names
                         (mapv #(hash-map :name %1) names))]
    (reset! channel-cur chan-tbl)))

;;TODO: should message propagate into each channel, or default or into active??
;;choosed active
(defmethod handle-message "ChannelCreated"
  [global-app-state msg]
  (let [new-ch-name (get msg "name")
        active-ch (get-in @global-app-state [:chat :active-channel] "main")
        channel-cur (cursor [:chat :channels active-ch] global-app-state)]
      ;add message notification
      (reset! channel-cur
              :messages (vec (cons msg
                                   (get @channel-cur :messages []))))
      ;add new channel into channels list
      (swap! global-app-state
             (fn [xs]
                (assoc-in xs [:chat :channels]
                          (merge
                            (get-in xs [:chat :channels] {})
                            {new-ch-name {:name new-ch-name
                                          :users []
                                          :messages []}}))))))

(defmethod handle-message "JoinChannel"
  [global-app-state msg]
  (let [ch-name (get msg "name")
        channel-cur (cursor [:chat :channels ch-name] global-app-state)]
    (.debug js/console "Joined channel:" (pr-str msg))
    ;update active channel
    (swap! global-app-state
           (fn [xs]
             (assoc-in xs [:chat :active-channel] ch-name)))
    ;update channel record
    (reset! channel-cur {:name ch-name
                         :users (vec (get msg "users"))
                         :messages (vec (get msg "messages" []))})))

(defmethod handle-message "Joined"
  [global-app-state msg]
  (let [ch-name (get msg "channel")
        new-user (get msg "user")
        channel-cur (cursor [:chat :channels ch-name] global-app-state)]
    (swap! channel-cur
           (fn [xs]
             (assoc xs
                    :users (vec (cons new-user (get xs :users [])))
                    :messages (vec (cons msg (get xs :messages []))))))))

(defmethod handle-message "Left"
  [global-app-state msg]
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

(defmethod handle-message "Msg"
  [global-app-state msg]
  (let [ch-name (get msg "channel")
        channel-cur (cursor [:chat :channels ch-name] global-app-state)]
    (swap! channel-cur
           (fn [xs]
             (assoc xs :messages
                    (vec
                      (conj (:messages xs)
                            (get msg "message"))))))))

(defmethod handle-message "Error"
  [global-app-state msg]
  (swap! global-app-state
         (fn [xs] (assoc xs :alert {:title "Server error"
                                    :message (get msg "error")}))))

(defmethod handle-message :default [_ msg]
  (.error js/console "Got unknown message" (pr-str msg)))

(defn send! [socket data]
  (let [w (transit/writer :json-verbose)]
    (.send socket (transit/write w data))))

(defn close! [socket]
  (.close socket))

;;-- commands
(defn join-channel
  [ws-socket chan-name]
  (send! ws-socket
         {"$variant" "JoinChannel"
          "name" (str chan-name)}))

(defn leave-channel
  [ws-socket chan-name]
  (send! ws-socket
         {"$variant" "LeaveChannel"
          "name" (str chan-name)}))

(defn register
  [ws-socket user-data]
  (send! ws-socket
         {"$variant" "Register"
          "user" user-data}))

;;TODO: add error handlers
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
              (register ws {"name" user-login})
              (join-channel ws (str last-channel))))
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


