(ns chat-client.connection
  (:require [cognitect.transit :as transit]
            [reagent.core :as reagent :refer [cursor]]))

(defonce statuses [:connecting :open :closing :closed :init])

(def channels (reagent/atom []))

(defmulti handle-message
  (fn [_ msg] (get msg "$variant" :default)))

(defmethod handle-message "Channels" [global-app-state msg]
  (let [chan-cur (cursor [:chat :channels] global-app-state)
        names (get msg "names")
        chan-tbl (zipmap names
                         (mapv #(hash-map :name %1) names))]
    (reset! chan-cur chan-tbl)
    ;(reagent/force-update-all)
    (.log js/console "New channels: " (pr-str @chan-cur))
    @chan-cur))

(defmethod handle-message "JoinChannel" [global-app-state msg]
  (let [ch-name (get msg "name")
        ch-cur (cursor [:chat :channels ch-name] global-app-state)]
    (.log js/console "Joined on the channel: " (pr-str msg))
    (reset! ch-cur {:name ch-name
                    :users (get msg "users")
                    :messages (get msg "messages")})
    ;(reagent/force-update-all)
    ))

(defmethod handle-message :default [_ msg]
  (.error js/console "Got unknown message" (pr-str msg)))

(defn send! [socket data]
  (let [w (transit/writer :json-verbose)]
    (.send socket (transit/write w data))))

(defn close! [socket]
  (.close socket))


(defn create
  [global-app-state]
  (let [status-cur (cursor [:connection :status] global-app-state)]
  (letfn [(on-error [ev]
            (.error js/console "Connection error: " ev)
            (reset! status-cur :closed))
          (on-open [ev]
            (let [ws (.-target ev)
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
            (.debug js/console "Connection is closed." ev)
            (reset! status-cur :open))]
    (let [url (get-in @global-app-state [:connection :url])
          ws (js/WebSocket. url)]
      (set! (.-onerror ws) on-error)
      (set! (.-onopen ws) on-open)
      (set! (.-onmessage ws) on-message)
      (set! (.-onclose ws) on-close)
      ws))))



