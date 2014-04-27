(ns bacon-async.react
  (:require [sablono.core :as html
             :refer-macros [html]]
            [bacon-async.core :as b]))

(enable-console-print!)

(defrecord Component [elem in out])

(def component map->Component)

(defn react-elem [obs]
  (let [current-markup (atom [:div])
        c (js/React.createClass
            (clj->js
              {:render
               (fn []
                 (html @current-markup))
               :componentDidMount
               (fn []
                 (this-as this
                          (b/on-value!
                            obs
                            (fn [markup]
                              (reset! current-markup markup)
                              (.forceUpdate this)))))}))]
    (c)))

(defn init-react-test []
  (let [in (-> (b/sequentially 1000 [[:div "Hello, World!"]
                                     [:div "The world has changed!"]])
               (b/merge (b/constant [:div])))
        elem (react-elem in)]
    (component
      {:elem elem})))

(defn ^:export run []
  (let [react-test (init-react-test)
        in (b/map
             (b/sequentially 200 [1 2 3])
             (fn [i]
               [:div (str "YO " i)
                (:elem react-test)]))
        my-component (react-elem in)]
    (js/React.renderComponent my-component (.-body js/document))))
