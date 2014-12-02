(ns chat-client.utils)

;;various helperfunction
(defn by-selector
  ([selector]
    (by-selector js/document selector))
  ([doc-node selector]
    (.querySelector doc-node (str selector))))


;;ps: it's expect that moment is loaded before app itself;
(defn time-ago [dt]
  "returns string of times ago in human readable form"
  (str (.fromNow (js/moment. dt))))

