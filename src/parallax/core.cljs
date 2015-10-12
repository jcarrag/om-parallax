(ns ^:figwheel-always parallax.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(defonce app-state (atom {}))

(defn handle-scroll
  [owner _]
  (let [node (om/get-node owner)]
    (om/set-state! owner :current-height (.-scrollTop node))))

(defn fade-effect
  [current-height client-height]
  (- 1 (* 1.5 (/ current-height client-height))))

(defn scroller
  [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:client-height  (.. js/window -innerHeight)
       :client-width   (.. js/window -innerWidth)
       :current-height 0})

    om/IRenderState
    (render-state [_ {:keys [client-height current-height client-width]}]
      (dom/div #js {:style     #js {:position  "relative"
                                    :height    "100vh"
                                    :overflowY "scroll"}
                    :className "outer-div"
                    :onScroll  (partial handle-scroll owner)}

        ; Fade in/out effect
        (dom/div #js {:className "boat"
                      :style     #js {:position             "absolute"
                                      :top                  current-height
                                      :width                client-width
                                      :height               client-height
                                      :background           "url(images/bcg-slide-1.jpg) no-repeat center"
                                      :WebkitBackgroundSize "cover"
                                      :MozBackgroundSize    "cover"
                                      :OBackgroundSize      "cover"
                                      :backgroundSize       "cover"}}

          ; Scrolling message
          (dom/div #js {:style #js {:position  "fixed"
                                    :top       (/ (- (/ client-height 1.5) current-height)
                                                  2)
                                    :width     client-width
                                    :opacity   (fade-effect current-height client-height)
                                    :textAlign "center"}}
            (dom/h1 #js {:style #js {:color      "#ffffff"
                                     :fontFamily "Open Sans, sans-serif"}}
                    "Parallax in Om")))

        ; Curtain effect
        (let [slide-height (* 1.5 client-height)]
          (dom/div #js {:className "grass"
                        :style     #js {:position             "absolute"
                                        :top                  client-height
                                        :width                client-width
                                        :height               slide-height
                                        :background           "url(images/bcg-slide-2.jpg) no-repeat center"
                                        :WebkitBackgroundSize "cover"
                                        :MozBackgroundSize    "cover"
                                        :OBackgroundSize      "cover"
                                        :backgroundSize       "cover"}}

            (when (>= current-height client-height) ; hardcoded to be second slide
              (dom/div #js {:className "curtain"
                            :style     #js {:width                client-width
                                            :height               (* (/ (- current-height client-height) ; hardcoded to be second slide
                                                                        (- slide-height client-height))
                                                                     slide-height)
                                            :opacity              (* 0.6 (/ (- current-height client-height) ; hardcoded to be second slide
                                                                            (- slide-height client-height)))
                                            :background           "#000000"
                                            :WebkitBackgroundSize "cover"
                                            :MozBackgroundSize    "cover"
                                            :OBackgroundSize      "cover"
                                            :backgroundSize       "cover"}}))))))

    om/IDidUpdate
    (did-update [_ _ {:keys [client-width client-height]}]
      ; TODO change this to be instant, instead of afer a render (channels)
      (let [node (om/get-node owner)]
        (when (not= client-height (.. js/window -innerHeight))
          (om/set-state! owner :client-height (.-clientHeight node)))
        (when (not= client-width (.. js/window -innerWidth))
          (om/set-state! owner :client-width (.-clientWidth node)))))))

(om/root
  scroller
  app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )