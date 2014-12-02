(ns chat-client.components.users
  (:require [reagent.core :as reagent :refer [cursor]]))

(defn make-user-item [user]
  [:div {:class "list-group-item user-list-item"}
    [:div {:class "row-action-primary"}
      [:i {:class "icon mdi-social-person"} " "]]
    [:div {:class "row-content"}
      [:div {:class "least-content"} "active"]
      [:h5 {:class "list-group-item-heading"}
        (str (get user "name"))]]])

(defn make-user-list [channel-cur]
  [:div {:class "list-group user-list"}
    (for [user (:users channel-cur)]
      (make-user-item user))])

(defn render
  [global-app-state]
  (let [current-channel (cursor [:chat :active-channel] global-app-state)
        current-channel-name (str @current-channel)
        channel-cur (cursor [:chat :channels current-channel-name] global-app-state)]
    [:div {:class "panel panel-primary"}
      [:div {:class "panel-heading"}
        [:h3 {:class "panel-title"}
          [:i {:class "icon mdi-social-people"} " "]
          "Users on the channel "
          [:strong (str @current-channel)]]]
      [:div {:class "panel-body"}
        (make-user-list @channel-cur)]]))
