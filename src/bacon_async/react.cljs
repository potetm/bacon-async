(ns bacon-async.react
  (:require [sablono.core :as html
             :refer-macros [html]]
            [bacon-async.core :as b]))

(defrecord Component [elem in out])

(def component map->Component)

(defn react-elem [obs]
  (let [current-markup (atom [:div])
        c (js/React.createClass
            (clj->js
              {:render
               (fn []
                 (html @current-markup))}))]
    (b/on-value! obs (partial reset! current-markup))
    (c)))

(defn render-component [obs component container]
  (let [comp (js/React.renderComponent component container)]
    (b/on-value! obs #(.forceUpdate comp))))

(defn init-react-test []
  (let [in (-> (b/sequentially 1000 [[:div "Hello, World!"]
                                     [:div "The world has changed!"]])
               (b/merge (b/constant [:div])))
        elem (react-elem in)]
    (component
      {:elem elem
       :in in})))

(defn ^:export run []
  (let [react-test (init-react-test)
        in (b/map
             (:in react-test)
             (fn [elem]
               [:div "YO"
                elem]))
        my-component (react-elem in)]
    (render-component in my-component (.-body js/document))))
