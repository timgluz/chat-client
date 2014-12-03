(ns chat-client.components.alert
  (:require [reagent.core :refer [cursor] :as reagent]))

;;TODO: remove after x seconds
;;TODO: show more than 1
(defn render
  [global-app-state]
  (let [alert-cur (cursor [:alert] global-app-state)
        display? (not (empty? @alert-cur))]
    [:div {:class "alert alert-dismissable alert-warning"
           :style {:display (if display? "block" "none")}}
      [:button {:type "button"
                :class "close"
                :data-dismiss "alert"} "x"]
      [:h4 (:title @alert-cur)]
      [:p (:message @alert-cur)]]))
