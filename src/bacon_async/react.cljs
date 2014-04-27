(ns bacon-async.react
  (:require [sablono.core :as html
             :refer-macros [html]]
            [bacon-async.core :as b]))

(defn react
  ([obs]
   (react obs [:div]))
  ([obs initial-markup]
   (let [current-markup (atom initial-markup)
         c (js/React.createClass
             (clj->js
               {:render
                (fn []
                  (html @current-markup))}))]
     (b/on-value! obs (partial reset! current-markup))
     c)))

(defn render-component [obs initial-markup container]
  (let [c (react obs initial-markup)
        comp (js/React.renderComponent (c) container)]
    (b/on-value! obs #(.forceUpdate comp))))

(defn ^:export run []
  (-> (b/sequentially 1000 [[:div "Hello, World!"]
                            [:div "The world has changed!"]])
      (render-component [:div "YO"] (.-body js/document))))
