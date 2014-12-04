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
    (testing "renders empty list if no items"
      (println "#-- render-channel-component")

      (reagent/render-component
        [#(channels-app/render test-app-state)]
        test-el)
      (println (.-innerHTML test-el))
      (is (= 1 2))
      )))
