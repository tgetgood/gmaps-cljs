(ns gmaps.map.directions)

(def directions-renderers (atom {}))

(defn- directions-renderer
  "Returns the direction renderer for this map, creating a new one if
  necessary."
  [map-obj]
  (if-let [dr (get @directions-renderers map-obj)]
    dr
    (let [dr (google.maps.DirectionsRenderer.)]
      (swap! directions-renderers assoc map-obj dr)
      dr)))

(defn cleanup!
  "Allow DirectionRenderer associated with this map to be garbage
  collected."
  [map-obj]
  (swap! directions-renderers dissoc map-obj))

(defn set-directions!
  "Update the map to show the current directions."
  [map-obj directions]
  (let [dr (directions-renderer map-obj)]
    (if (nil? directions)
      (.setMap dr nil)
      (do
        (.setMap dr map-obj)
        (.setDirections dr directions)))))
