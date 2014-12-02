(ns chat-client.components.chat
  (:require [reagent.core :refer [cursor]]))

(defmulti make-message-item
  (fn [msg] (get msg "$variant")))

(defmethod make-message-item "Joined" [msg]
  [:div {:class "chat-item list-group-item"
         :style {:background-color "floralwhite"}}
    [:div {:class "row-action-primary"}
      [:i {:class "icon mdi-file-folder"} " "]]
    [:div {:class "row-content"}
      [:div {:class "least-content"}
        [:small " "
          (str (.fromNow
                 (js/moment. (get-in msg ["message" "timestamp"]))))]]
      [:h5
        {:class "list-group-item-heading"}
        "user X joined with the channel"]]])

(defmethod make-message-item "Msg" [msg]
  [:div {:class "chat-item list-group-item"
         :style {:background "white"}}
    [:div {:class "row-picture"}
      [:i {:class "icon mdi-social-person"} " "]]
     (let [msg-body (get msg "message")
          dt (js/Date. (get msg-body "timestamp"))]
      [:div {:class "row-content"}
        [:div {:class "least-content"}
          [:small "last update "
            (str (.fromNow (js/moment. dt)))]]
        [:div
          {:class "list-group-item-heading"}
          (get-in msg-body ["user" "name"] "unknown")]
        [:p {:class "list-group-item-text"}
          (str " > " (get msg-body "text"))]])])

(defmethod make-message-item :default [msg]
  (.error js/console "Unsupported message: " msg))


(defn make-chat-window [chat-dt]
  (let [active-channel (:active-channel @chat-dt)
        current-chat (get-in @chat-dt [:channels active-channel])]
    [:div {:class "chat-messages-container"}
      [:div {:class "list-group"}
       ;;NOTE: not perfect just take 100th latest msg from channel X.
       ;;NB! the order of adding new message matters - LIFO stack
       "#TODO: finish it"
       #_(into []
         (comp
           (filter
             (fn [msg] (= active-channel (get msg "channel"))))
           (take 100)
           (map #(make-message-item %)))
        (:messages current-chat))
       ]]))

(defn make-chat-form [chat-dt]
  (let [msg-cur (cursor [:sent] chat-dt)]
    [:div
      {:class "chat-form-container"}
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
                 :defaultValue (str (:message @msg-cur))
                 :on-change (fn [ev]
                            (swap! msg-cur
                                   (fn [xs]
                                     (assoc xs :message (.. ev -target -value)))))}]
              [:span {:class "help-block"}
                "Type your message and press `ENTER` to send your message."]]]
          [:div {:class "form-group"}
            [:div {:class "col-lg-10 col-lg-offset-2"}
              [:button
                {:class "btn btn-default"
                 :on-click (fn [ev]
                             (.preventDefault ev)
                             (swap! msg-cur
                                    (fn [xs] (assoc xs :message "" ))))}
                "Clear"]
              [:button
                {:class "btn btn-primary"
                 :on-click (fn [ev]
                             (.preventDefault ev)
                             (swap! msg-cur
                                   (fn [xs] (assoc xs :status :sending))))
                 :type "submit"}
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
       (make-chat-window chat-dt)
       (make-chat-form chat-dt)
       ]]))

