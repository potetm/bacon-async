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
  (let [in (b/bus)
        val (b/bus)
        html (-> in
                 (b/map (partial + 100))
                 (b/map (fn [i]
                          [:div {:on-click #(b/push! val "testing!")} (str "Testing " i)]))
                 (b/merge (b/constant [:div]))
                 (b/merge (b/map val (fn [v] [:div (str "Forget yo " v)]))))
        elem (react-elem html)]
    (component
      {:elem elem
       :in in})))

(defn ^:export run []
  (let [react-test (init-react-test)
        in (b/map
             (b/sequentially 200 [1 2 3])
             (fn [i]
               [:div (str "YO " i)
                (:elem react-test)]))
        my-component (react-elem in)]
    (->> (b/sequentially 1000 [1 2 3 4 5])
         (b/plug! (:in react-test)))
    (js/React.renderComponent my-component (.-body js/document))))
