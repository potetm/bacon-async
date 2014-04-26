(ns bacon-async.core
  (:refer-clojure :exclude [filter map merge repeatedly take take-while])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [bacon-async.event :as e]
            [cljs.core.async :refer [>! <! alts! chan sliding-buffer put! timeout tap mult close!] :as async]))

(defn- subscribe-loop [src f]
  (let [t (tap (mult src) (chan))]
    (go
      (loop [event (<! t)]
        (f event)
        (when-not (:end? event)
          (recur (<! t)))))))

(defprotocol ISubscribe
  (-subscribe! [obs f]))

(defrecord EventStream [src]
  ISubscribe
  (-subscribe! [_ f]
    (subscribe-loop src f)))

(defn eventstream [src]
  (->EventStream src))

(defrecord Property [src current-val-atom]
  ISubscribe
  (-subscribe! [_ f]
    (let [current-val @current-val-atom]
      (when-not (= ::none current-val)
        (f (e/initial current-val)))
      (subscribe-loop src f))))

(defn to-property [obs]
  (->Property (:src obs) (atom ::none)))

(defn subscribe! [obs f]
  (-subscribe! obs f))

(defn sequentially [delay vals]
  (let [out (chan)]
    (go
      (doseq [v vals]
        (<! (timeout delay))
        (>! out (e/next v)))
      (>! out (e/end)))
    (eventstream out)))

(defn later [delay val]
  (sequentially delay [val]))

(defn from-array [vals]
  (let [out (chan)]
    (go
      (doseq [v vals]
        (>! out (e/next v)))
      (>! out (e/end)))
    (eventstream out)))

(defn merge [left right]
  (let [end? (atom false)
        in (async/merge [(:src left) (:src right)])
        out (chan)]
    (go
      (loop [event (<! in)]
        (if (:end? event)
          (if @end?
            (>! out event)
            (reset! end? true))
          (>! out event))
        (recur (<! in))))
    (eventstream out)))
