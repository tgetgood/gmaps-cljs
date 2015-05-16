# Gmaps-cljs

A functional wrapper for the Google Maps API.

Have you ever wished Google Maps played better with Om? I do. This
library strives to let you pretend a map is an immediate mode
renderer, much the same as React's virtual dom lets you treat the real
dom.

Declare what you want to display as a data structure and update the
map. Gmaps-cljs tries to intelligently update the map by diffing the
current state with the new state you gave it. No more manually keeping
track of which markers are hidden, etc..

This started off as a port of
[Gmaps.js](https://hpneo.github.io/gmaps/) but subsequently became
much more ambitious.

## Usage

Define the content of the page as a map

```clojure
(def map-data
  {:center {:lat 0 :lng 0}
   :disableDefaultUI true
   :zoom 12
   :mapTypeId google.maps.MapTypeId.ROADMAP

   :markers #{{:lat 0
               :lng 0
               :title "Nexus of the universe"}}}
```

then define a component (example using Om)

```clojure
(ns example
  (:require [gmaps-cljs.maps-wrapper :as mw]))

(defcomponent map-view [app owner]
  (did-mount [_]
    (let [map-data (om/get-props owner)]
      (mw/attach-map! (om/get-node owner "map-canvas") map-data)))
  (did-update [_ props state]
    (mw/update-map! (om/get-node owner "map-canvas") props))
  (will-unmount [_]
    ;; Release the google.maps.Map for this component
    (mw/detach-map! (om/get-node owner "map-canvas")))
  (render [_]
    (dom/div {:id "map-canvas" :ref "map-canvas"})))
```
 
and attach it to the dom

```clojure
(om/root map-view (mw/create-map map-data)
  {:target (.-body js/document)})
```

create-map is a helper function which just adds defaults and
pre-processes the map state. It will be internalised in a future
update.

## Working

* The map embeds!
* Markers
* Directions (mostly)

## TODO

* Callbacks on markers
* Polygons
* Reconsider all names
* Simpler API
* 

## License

Copyright © 2015 Thomas Getgood

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
