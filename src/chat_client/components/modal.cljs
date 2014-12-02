(ns chat-client.components.modal
  (:require [reagent.core :refer [cursor] :as reagent]
            [chat-client.connection :as connection]))

(defn show-connection-form
  [global-app-state]
  (let [conn-cur (cursor [:connection] global-app-state)]
    [:form {:class "form-horizontal"}
      [:fieldset
        [:legend "Enter your credentials"]
        [:div {:class "form-group"}
          [:label {:for "url" :class "col-lg-2 control-label"} "Url"]
          [:div {:class "col-lg-10"}
            [:input
              {:type "text" :class "form-control"
               :id "url" :placeholder "Url:"
               :defaultValue (str (:url @conn-cur))
               :on-change (fn [ev]
                           (swap! conn-cur
                                  (fn [xs]
                                    (assoc xs :url (.. ev -target -value)))))}]]]
        [:div {:class "form-group"}
          [:label {:for "username" :class "col-lg-2 control-label"} "Username"]
          [:div {:class "col-lg-10"}
            [:input
              {:type "text" :class "form-control"
               :id "username" :placeholder "Username:"
               :defaultValue (str (:username @conn-cur))
               :on-change (fn [ev]
                            (let [new-val (-> ev .-target .-value)]
                              (.log js/console "Changed username: " new-val)
                              (reset! conn-cur (assoc @conn-cur :username new-val))
                              (reagent/force-update-all)))}]]
          ]]]))

(defn render
  [global-app-state]
  (let [conn-cur (cursor [:connection] global-app-state)]
    [:div {:class "modal"
           :style {:display (if (= :open (:status @conn-cur))
                                "none"
                                "block")}}
      [:div {:class "modal-dialog"}
        [:div
          {:class "modal-content"}
          [:div
            {:class "modal-header"}
            [:button
              {:type "button" :class "close"
               :data-dismiss "modal" :aria-hidden "true"
               :on-click (fn [ev]
                          (swap! conn-cur
                                 (fn [xs]
                                   (assoc xs :status :closed))))}
              "x"]
            [:h4 {:class "modal-title"} "Connect to chat"]]
          [:div
            {:class "modal-body"}
            (show-connection-form global-app-state)]
          [:div
            {:class "modal-footer"}
            [:button
              {:type "button" :class "btn btn-default" :data-dismiss "modal"
               :on-click (fn [ev]
                          (swap! conn-cur
                                 (fn [xs]
                                   (assoc xs :status :closed))))}
              "Cancel"]
            [:button
              {:type "button" :class "btn btn-primary"
               :on-click (fn [ev]
                           (.debug js/console "Initializing new connection.")
                           (swap! conn-cur
                                  (fn [xs]
                                    (assoc xs
                                           :status :init
                                           :socket (connection/create global-app-state)))))}
              "Connect"]]]]]))

