(ns bacon-async.react
  (:require [sablono.core :as html
             :refer-macros [html]]))

(defn component []
  (js/React.createClass
    (clj->js
      {:render
       (fn []
         (this-as this
                  (html
                    [:div "Hello, World!"])))})))

(defn render-component [component container]
  (js/React.renderComponent (component) container))

(defn ^:export run []
  (render-component
    (component)
    (.-body js/document)))
