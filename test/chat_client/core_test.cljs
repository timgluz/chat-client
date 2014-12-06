(ns chat-client.core-test
  (:require-macros
    [cemerick.cljs.test :refer [is deftest testing test-var
                                run-tests with-test]])
  (:require [cemerick.cljs.test :as t]
            [reagent.core :as reagent :refer [atom]]
            [chat-client.components.channels :as channels-app]))

(deftest demotest
  (testing "passes"
    (is (= 1 1))))

(defn by-id [node-id]
  (.getElementById js/document by-id))

(defn by-sel [selector]
  (.querySelector js/document selector))

(defn create-container
  [node-id]
  (let [new-node (.createElement js/document "div")]
    (.setAttribute new-node "id" node-id)
    (.appendChild js/document.body new-node)
    (by-id node-id)))


(deftest render-channel-component
  (let [test-app-state (atom {:chat {:channels {}}})
        test-el (by-sel "body")]
    (println "#-- render-channel-component")
    (reagent/render-component
      [#(channels-app/render test-app-state)]
      test-el)

    (testing "renders empty list if no items"
      (is (= "No channels."
             (re-find #"No channels." (.-innerHTML test-el)))))
    (testing "renders a list of active channels"
      (reset! test-app-state {:chat {:active-channel "main"
                                     :channels {"main" {:name "main"
                                                        :users []}}}})
      (reagent/force-update-all)
      (is (= "main"
             (re-find #"main" (.-innerHTML test-el)))))

    (reagent/unmount-component-at-node test-el)))
