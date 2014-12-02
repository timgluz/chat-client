(ns chat-client.components.channels
  (:require [reagent.cursor :refer [cursor]]))

(defn make-channel-item
  [channel]
  [:div {:class "channel-list-item list-group-item"}
    [:div {:class "row-action-primary"}
      [:i {:class "icon mdi-action-group-work material-blue"} " "]]
    [:div {:class "row-content"}
      [:h5
        {:class "list-group-item-heading"}
        (str (:name channel))]]])

(defn render
  [global-app-state]
  (let [cur-path [:chat :channels]
        channels-cur (cursor cur-path global-app-state)]
    (.log js/console "rendering channels lists")
    [:div {:class "panel panel-primary"}
        [:div {:class "panel-heading"}
          [:h3 {:class "panel-title"}
            [:i {:class "icon mdi-action-speaker-notes pull-left"} " "]
            "Channels: "
            (str (count @channels-cur))
            ]]
        [:div {:class "panel-body"}
          [:div {:class "list-group channel-list"}
            (for [channel (vals @channels-cur)]
              (make-channel-item channel))]]]))

