(ns gmaps.maps-data
   (:require-macros [cljs.core.async.macros :refer [go alt!]])
   (:require [cljs.core.async :refer [>! <! chan]]))

(defn index-of 
  "Returns first index in coll at which v occurs. Nil if not found. Result
  won't make sense for non-seqs"
  [coll v]
  (let [i (count (take-while #(not= % v) coll))]
    (when (or ( < i (count coll)) (= v (last coll)))
      i)))

(defn- maybe-retry
  [stat f & args]
  (when (= stat "OVER_QUERY_LIMIT")
    (js/setTimeout
      (fn [] (apply f args))
      10000)))

(defn get-geocode
  ([loc]
   (let [out (chan)]
     (get-geocode loc out)))
  ([loc out]
   (let [gc  (google.maps.Geocoder.)
         address (clj->js {:address loc})
         cb  (fn [res status]
               (if (= status "OK")
                 (go (>! out (first res)))
                 (maybe-retry status get-geocode loc out)))]
     (.geocode gc address cb)
     out)))

(defn init-map
  [loc elem]
  (let [geoc (get-geocode loc)
        out  (chan)]
    (go
      (let [coords (<! geoc)
            opts {:disableDefaultUI true
                  :zoom 13
                  :center (-> coords .-geometry .-location)
                  :mapTypeId google.maps.MapTypeId.ROADMAP}
            m    (google.maps.Map. elem (clj->js opts))]
        (>! out m)))
    out))

(defn same-place
  [gll ll]
  (and (not (nil? gll))
       (= (:lat ll) (.lat gll))
       (= (:lng ll) (.lng gll))))

(defn goog-lat-lng
  [{:keys [lat lng]}]
  (google.maps.LatLng. lat lng))

(defn google-geocode-to-location
  [geo]
  (let [pos (-> geo .-geometry .-location)]
    {:lat (.-k pos) :lng (.-B pos)}))

(defn create-marker
  [loc loc-name]
  (let [lat-lng (goog-lat-lng loc)
        opts {:position lat-lng
              :map nil
              :title loc-name}]
    (google.maps.Marker. (clj->js opts))))

(defn get-distances
  [me dests]
  (let [out  (chan (quot (count dests) 25))
        opts {:origins [me]
              :destinations (map #(get % "name") dests)
              :travelMode google.maps.TravelMode.WALKING,
              :unitSystem google.maps.UnitSystem.METRIC}
        dm   (google.maps.DistanceMatrixService.)]
    (.getDistanceMatrix dm (clj->js opts)
                        (fn [r s] (go (>! out r))))
    out))

(defn get-location
  [site]
  (let [out (chan)]
    (go (let [sitename (:name site)
              geo (<! (get-geocode sitename))]
          (>! out {:sitename sitename 
                   :location (google-geocode-to-location geo)})))
    out))

; (defrecord DirectionsRequest [:origin :destination :travelMode :unitSystem])
; 
; (defn directions-request
;   [{:keys [:origin :destination :travelMode :unitSystem] :or
;    {:travelMode google.maps.TravelMode.DRIVING 
;     :unitSystem google.maps.UnitSystem.METRIC}}]
;   (DirectionsRequest. origin destination travelMode unitSystem))

(def directions-service (atom nil))

(defn get-directions
  [opts]
  (compare-and-set! directions-service nil (google.maps.DirectionsService.))
  (let [out (chan)
        popts (assoc opts :origin (goog-lat-lng (:origin opts)))]
    (.route @directions-service (clj->js popts)
            (fn [res stat]
              (if (= stat "OK")
                (go (>! out res))
                (maybe-retry stat get-directions opts))))
    out))


