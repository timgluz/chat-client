(ns chat-client.components.status
  (:require [reagent.core :as reagent :refer [cursor]]))

(defn render
  [global-app-state]
  (let [conn-cur (cursor [:connection] global-app-state)]
    [:div {:class "panel panel-info"}
      [:div {:class "panel-heading"}
        [:h3 {:class "panel-title"}
          [:i {:class "icon mdi-communication-phone pull-left"}]
          "Connection status"]]
      [:div {:class "panel-body"}
        [:p
          [:strong "status: "]
          (str (:status @conn-cur))]
        [:p
          [:strong "url: "]
          (str (:url @conn-cur))]
        [:p
          [:strong "username: "]
          (str (:username @conn-cur))]]]))

