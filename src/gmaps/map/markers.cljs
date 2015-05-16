(ns gmaps.map.markers
  (:require [gmaps.location :as loc]))


(defn create-marker
  [loc loc-name]
  (let [lat-lng (loc/lat-lng loc)
        opts {:position lat-lng
              :map nil
              :title loc-name}]
    (google.maps.Marker. (clj->js opts))))
