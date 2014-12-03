(ns chat-client.components.channels
  (:require ;[reagent.cursor :refer [cursor]]
            [reagent.core :refer [atom cursor]]
            [chat-client.connection :as conn]))


(def is-form-visible (atom false))

(defn new-channel-button []
  [:span {:class "pull-right"}
    [:button {:class "button button-action"
              :title "Add new channel"
              :on-click #(swap! is-form-visible not)}
      [:i {:class "mdi-content-add-circle-outline"} " "]]])

(defn inline-form  [{:keys  [value on-save on-stop]}]
    (let  [val  (atom value)
           stop #(do
                   (reset! val  "")
                    (if on-stop  (on-stop)))
           save #(let  [v  (-> @val str clojure.string/trim)]
                   (if-not  (empty? v)  (on-save v))
                   (stop))]
      (fn  [props]
        [:div {:class "form-group"}
          [:input  (merge props
                         {:class "form-control col-lg-1 col-md-1"
                          :type  "text" :value @val ;:on-blur save
                          :on-change #(reset! val  (-> % .-target .-value))
                          :on-key-up #(case  (.-which %)
                                        13  (save)
                                        27  (stop)
                                        nil)})]
            [:button {:class "btn btn-primary"
                      :type "button"
                      :on-click save}
              "Ok"]])))

(defn new-channel-form [ws-socket active-channel]
  (let [new-chan-name (atom "new-channel1")]
    [:div {:class "form-group"
           :style {:display (if @is-form-visible "block" "none")}}
      [:label {:class "control-label"} "A name of the new channel"]
      [(inline-form {:value "default_room"
                     :on-stop #(reset! is-form-visible false)
                     :on-save (fn [v]
                                (.log js/console "Going to create new room: " v)
                                (reset! is-form-visible false)

                                (.log js/console "Leaving " active-channel)
                                (conn/leave-channel ws-socket active-channel)
                                (conn/join-channel ws-socket v))})]]))

;;TODO: should we keep track on all channels - server fails when user already on ch
(defn make-channel-item
  [channel active-channel ws-socket]
  [:div {:class "channel-list-item list-group-item"}
    [:div {:class "row-content1"}
      (if (= active-channel (:name channel))
        [:button {:class "btn btn-block btn-default disabled"}
          [:span
            [:i {:class "icon mdi-action-group-work"} " "]
            (str (:name channel))
            [:small " - current"]]]
        ;button for inactive rooms
        [:button {:class "btn btn-block btn-default"
                  :on-click (fn []
                              (let [ch-name (:name channel)]
                                (.debug js/console "Logging into room: " ch-name)
                                (conn/leave-channel ws-socket active-channel)
                                (conn/join-channel ws-socket ch-name)))}
          [:span
            [:i {:class "icon mdi-action-group-work material-blue"} " "]
            (str (:name channel))]])]])

(defn render
  [global-app-state]
  (let [ws-socket (get-in @global-app-state [:connection :socket])
        active-channel (get-in @global-app-state [:chat :active-channel])
        channels-cur (cursor [:chat :channels] global-app-state)]
    [:div {:class "panel panel-primary"}
        [:div {:class "panel-heading"}
          [:h3 {:class "panel-title"}
            [:i {:class "icon mdi-action-speaker-notes pull-left"} " "]
            "Channels: " (str (count @channels-cur))
            (new-channel-button)]]
        [:div {:class "panel-body"}
          [:div {:class "new-channel-container"}
            (new-channel-form ws-socket)]

          (if (empty? @channels-cur)
            [:div {:class ""}
              [:p "No channels."]]
            [:div {:class "list-group channel-list"}
              (for [channel (vals @channels-cur)]
                (make-channel-item channel active-channel ws-socket))])]]))

