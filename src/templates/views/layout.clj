(ns templates.views.layout
  (:use [hiccup.page :only (html5 include-css include-js)])
  (:use [templates.views.content  :as c]))


(defn app [title & content]
  (html5 {:ng-app "myApp" :lang "en"}
         [:head
          [:meta {"name" "viewport" "content" "width=device-width, initial-scale=1"}]
          [:title title]
          ;; (include-css "//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-combined.min.css")
          ;; (include-js "http://code.angularjs.org/1.2.3/angular.min.js")
          ;; (include-js "js/ui-bootstrap-tpls-0.7.0.min.js")
          ;; (include-js "js/script.js")
          (include-css "css/style.css")
          (include-css "css/desert.css")
          (include-js "https://cdn.rawgit.com/google/code-prettify/master/loader/run_prettify.js")
          (include-js "js/lang-clj.js")
          [:body
           [:div {:id "main-content"}  (c/make-section (get-query :top) "top") content]]])
  )

(defn scrape-layout [title & content]
  (html5 {:ng-app "myApp" :lang "en"}
         [:head
          [:meta {"name" "viewport" "content" "width=device-width, initial-scale=1"}]
          [:title title]
          (include-css "css/style.css")
          (include-css "css/desert.css")
          (include-js "https://cdn.rawgit.com/google/code-prettify/master/loader/run_prettify.js")
          (include-js "js/lang-clj.js")
          [:body
           [:div {:id "main-content"}  (c/scrape-page)]]])
  )
