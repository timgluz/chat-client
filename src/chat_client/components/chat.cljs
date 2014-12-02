(ns chat-client.components.chat
  (:require [reagent.core :as reagent :refer [cursor]]
            [chat-client.connection :as connection]
            [chat-client.utils :refer [time-ago by-selector]]))

(def $ by-selector)

(defmulti make-message-item
  (fn [msg] (get msg "$variant")))

(defmethod make-message-item "Joined" [msg]
  [:div {:class "chat-item list-group-item"
         :style {:background-color "floralwhite"}}
    [:div {:class "row-action-primary"}
      [:i {:class "icon mdi-file-folder"} " "]]
    [:div {:class "row-content"}
      [:div {:class "least-content"}
        [:small " " (time-ago (js/moment.))]]
      [:h5
        {:class "list-group-item-heading"}
        (str "user " (get-in msg ["user" "name"])
             " joined with the channel")]]])

(defmethod make-message-item "Left" [msg]
  [:div {:class "chat-item list-group-item btn-material-bluegrey"}
    [:div {:class "row-action-primary"}
      [:i {:class "icon mdi-file-folder"} " "]]
    [:div {:class "row-content"}
      [:div {:class "least-content"}
        [:small " " (time-ago (js/moment.))]]
      [:h5 {:class "list-group-item-heading"}
        (str "User" (get-in msg ["user" "name"])
             " left the room.")]]])

(defmethod make-message-item :default [msg]
  [:div {:class "chat-item list-group-item"}
    [:div {:class "row-picture"}
      [:i {:class "icon mdi-social-person"} " "]]
    [:div {:class "row-content"}
      [:div {:class "least-content"}
        [:small (time-ago (get msg "stamp"))]]
      [:div
        {:class "list-group-item-heading"}
        (get-in msg ["user" "name"] "unknown")]
      [:p {:class "list-group-item-text"}
        (str " > " (get msg "text"))]]])

(defn scrolled-to-end [elems]
  (with-meta (fn [] elems)
    {:component-did-mount #(
                             (let [el (reagent/dom-node %)
                                   parent-el (.-parentNode el)]
                              (.debug js/console "Scrolled component to end ...")
                              (set! (.-scrollTop parent-el)
                                    (.-scrollHeight parent-el))))}))

(defn make-chat-window
  [global-app-state]
  (let [chat-cur (cursor [:chat] global-app-state)
        active-channel (:active-channel @chat-cur)
        current-chat (get-in @chat-cur [:channels active-channel])
        chat-msgs (map #(make-message-item %)
                       (:messages current-chat))]
      [:div {:class "chat-messages-container"}
          [:a {:name "chat-list-start"}]
          (if (empty? chat-msgs)
            [:div {:class "chat-item"} "No messages."]
            [(scrolled-to-end
              [:div {:class "list-group"} chat-msgs])])
          [:a {:name "chat-list-end"}]]
      ))


(defn make-chat-form [global-app-state]
  (let [active-channel (get-in @global-app-state [:chat :active-channel])
        msg-cur (atom "")
        send-message (fn [msg]
                       (if-let [ws-socket (get-in @global-app-state [:connection :socket])]
                         (connection/send! ws-socket
                                           {"$variant" "Msg"
                                            "channel" active-channel
                                            "message" msg})
                         (.error js/console "Not connected - cant send message.")))]
    [:div {:class "chat-form-container"}
      [:form {:class "form-horizontal"}
        [:fieldset
          [:div {:class "form-group"}
            [:label {:for "new-message-input"
                     :class "col-lg-2 control-label"}
             "New message:"]
            [:div {:class "col-lg-10"}
              [:textarea
                {:id "new-message-input"
                 :name "new-message-input"
                 :class "form-control"
                 :rows 3
                 :defaultValue (str @msg-cur)
                 :on-change (fn [ev]
                              (reset! msg-cur (.. ev -target -value)))}]
              [:span {:class "help-block"}
                "Type your message and press `ENTER` to send your message."]]]
          [:div {:class "form-group"}
            [:div {:class "col-lg-10 col-lg-offset-2"}
              [:button
                {:class "btn btn-default"
                 :on-click (fn [ev]
                             (.preventDefault ev)
                             (reset! msg-cur ""))}
                "Clear"]
              [:button
                {:class "btn btn-primary"
                 :on-click (fn [ev]
                             (.preventDefault ev)
                             ;;TODO: if sending fails
                             (send-message @msg-cur)
                             (reset! msg-cur ""))
                 :type "button"}
               "Send message"]]]]]]))


(defn render
  [global-app-state]
  (let [chat-dt (cursor [:chat] global-app-state)]
    [:div {:class "panel panel-success"}
      [:div {:class "panel-heading"}
        [:h3 {:class "panel-title"}
          [:i {:class "icon mdi-action-speaker-notes"} " "]
          "Chat"]]
      [:div {:class "panel-body"
             :style {:height "100%"}}
       (make-chat-window global-app-state)
       (make-chat-form global-app-state)]]))

