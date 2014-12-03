(ns chat-client.core
  (:require [reagent.core :as reagent]
            [chat-client.components.modal :as modal-app]
            [chat-client.components.status :as status-app]
            [chat-client.components.channels :as channels-app]
            [chat-client.components.chat :as chat-app]
            [chat-client.components.users :as users-app]
            [chat-client.components.alert :as alert-app]
            [chat-client.utils :refer [by-selector]]))

(defonce app-state (reagent/atom
                     {:alert {:title "Error"
                              :message "Demo error - bla bla!"}
                      :connection {:status :closed
                                   :url "ws://127.0.0.1:9000/chat"
                                   :username (str  "unknown" (rand-int 1000))
                                   :socket nil}
                      :chat {:active-channel "main"
                             :channels {}}}))

(defn ^:export main []
  (let [app-mount-points [[".alert-app" alert-app/render]
                          [".connection-modal" modal-app/render]
                          [".status-app" status-app/render]
                          [".channels-app" channels-app/render]
                          [".users-app" users-app/render]
                          [".chat-app" chat-app/render]]]
    ;;--mount apps
    (doseq [[selector app-renderer] app-mount-points]
      (reagent/render-component
        [(fn [] (app-renderer app-state))]
        (by-selector selector)))))

(main)
