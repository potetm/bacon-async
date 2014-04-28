(ns bacon-async.core
  (:refer-clojure :exclude [filter map merge repeatedly take take-while])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [bacon-async.event :as e]
            [cljs.core.async :refer [>! <! alts! chan timeout tap mult close! pipe] :as async]))

(defprotocol ISubscribe
  (-subscribe! [obs f]))

(defprotocol IProperty
  (-changes [prop]))

(defprotocol IBus
  (-plug! [bus obs])
  (-push! [bus val])
  (-error! [bus msg])
  (-end! [bus]))

(defn subscribe! [obs f]
  (-subscribe! obs f))

(defn on-value! [es f]
  (subscribe!
    es
    (fn [event]
      (when (:has-value? event)
        (f (:value event))))))

(defn- event-stream-subscribe [-mult f]
  (let [t (tap -mult (chan))]
    (go
      (loop [event (<! t)]
        (f event)
        (when-not (:end? event)
          (recur (<! t)))))))

(defrecord EventStream [src -mult]
  ISubscribe
  (-subscribe! [_ f]
    (event-stream-subscribe -mult f)))

(defn eventstream [src]
  (->EventStream src (mult src)))

(defrecord Property [src -mult current-val-atom]
  ISubscribe
  (-subscribe! [_ f]
    (let [current-val @current-val-atom]
      (when-not (= ::none current-val)
        (f (e/initial current-val)))
      (let [t (tap -mult (chan))]
        (go
          (loop [event (<! t)]
            (when (:has-value? event)
              (reset! current-val-atom (:value event)))
            (f event)
            (when-not (:end? event)
              (recur (<! t))))))))
  IProperty
  (-changes [prop]
    (let [out (chan)]
      (go
        (subscribe! prop
                    (fn [event]
                      (when-not (:initial? event)
                        (>! out event)))))
      (eventstream out))))

(defn property [src]
  (->Property src (mult src) (atom ::none)))

(defn tap-src [obs]
  (tap (:-mult obs) (chan)))

(defrecord Bus [src -mult]
  ISubscribe
  (-subscribe! [_ f]
    (event-stream-subscribe -mult f))
  IBus
  (-plug! [bus obs]
    (pipe (tap-src obs) (:src bus)))
  (-push! [bus val]
    (go (>! (:src bus) (e/next val))))
  (-error! [bus msg]
    (go (>! (:src bus) (e/error))))
  (-end! [bus]
    (go (>! (:src bus) (e/end)))))

(defn bus []
  (let [c (chan)]
    (->Bus c (mult c))))

(defn plug! [bus obs]
  (-plug! bus obs))

(defn push! [bus val]
  (-push! bus val))

(defn error! [bus msg]
  (-error! bus msg))

(defn end! [bus]
  (-end! bus))

(defn to-property [obs]
  (property (tap-src obs)))

(defn changes [prop]
  (-changes prop))

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

(defn repeatedly [delay values]
  (let [out (chan)]
    (go
      (doseq [v (cycle values)]
        (<! (timeout delay))
        (>! out (e/next v))))
    (eventstream out)))

(defn constant [val]
  (let [out (chan)]
    (go
      (>! out (e/initial val))
      (>! out (e/end)))
    (property out)))

(defn merge [left right]
  (let [end? (atom false)
        in (async/merge [(tap-src left) (tap-src right)])
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

(defn map [obs f]
  (eventstream (async/map #(e/map-event % f)
                          [(tap-src obs)])))

(defn filter [obs pred]
  (eventstream (async/filter< #(or (:end? %) (pred (:value %)))
                              (tap-src obs))))

(defn take [obs n]
  (let [out (chan)
        src (tap-src obs)]
    (go
      (dotimes [_ n]
        (>! out (<! src)))
      (>! out (e/end)))
    (eventstream out)))

(defn take-while [obs pred]
  (let [out (chan)
        src (tap-src obs)]
    (go
      (loop [event (<! src)]
        (when (or (:end? event) (pred (:value event)))
          (>! out event)
          (recur (<! src)))
        (>! out (e/end))))
    (eventstream out)))
