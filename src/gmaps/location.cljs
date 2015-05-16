(ns gmaps.location)

(defn same-place?
  [gll ll]
  (and (not (nil? gll))
       (= (:lat ll) (.lat gll))
       (= (:lng ll) (.lng gll))))

(defn lat-lng
  [{:keys [lat lng]}]
  (google.maps.LatLng. lat lng))

(defn geocode-to-location
  [geo]
  (let [pos (-> geo .-geometry .-location)]
    {:lat (.-k pos) :lng (.-B pos)}))

