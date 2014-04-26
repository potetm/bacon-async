(ns bacon-async.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [bacon-async.event :as e]
            [cljs.core.async :refer [>! <! alts! chan sliding-buffer put! timeout tap mult]]))

(defn subscribe! [c f]
  (let [t (tap (mult c) (chan))]
    (go
      (loop [event (<! t)]
        (f event)
        (when-not (:end? event)
          (recur (<! t)))))))

(defn sequentially [delay vals]
  (let [out (chan)]
    (go
      (doseq [v vals]
        (<! (timeout delay))
        (>! out (e/next v)))
      (>! out (e/end)))
    out))

(defn later [delay val]
  (let [out (chan)]
    (go
      (<! (timeout delay))
      (>! out (e/next val))
      (>! out (e/end)))
    out))
