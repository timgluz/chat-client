(ns chat-client.core
  (:require [reagent.core :as reagent]
            [chat-client.components.modal :as modal-app]
            [chat-client.components.status :as status-app]
            [chat-client.components.channels :as channels-app]
            [chat-client.components.chat :as chat-app]
            [chat-client.components.users :as users-app]
            ))

(defn $
  ([selector]
    ($ js/document selector))
  ([doc-node selector]
    (.querySelector doc-node (str selector))))

(defonce app-state (reagent/atom
                     {:connection {:status :closed
                                   :url "ws://127.0.0.1:9000/chat"
                                   :username (str  "unknown" (rand-int 1000))
                                   :socket nil}
                      :chat {
                        :active-channel "main"
                        :channels {}
                        :sent {:status :unsent
                               :message "<type your message>"
                               :timestamp 1001}}}))

(defn ^:export main []
  (let [app-mount-points [
                          [".connection-modal" modal-app/render]
                          [".status-app" status-app/render]
                          [".channels-app" channels-app/render]
                          [".users-app" users-app/render]
                          [".chat-app" chat-app/render]
                          ]]
    ;;--mount apps
    (doseq [[selector app-renderer] app-mount-points]
      (reagent/render-component
        (fn [] (app-renderer app-state))
        ($ selector) ))

    ;(reagent/render-component #(channels-app/render app-state) ($ ".channels-app"))

    ))

(main)
