(ns gmaps.services
   (:require-macros [cljs.core.async.macros :refer [go]])
   (:require [cljs.core.async :refer [>! <! chan put!]]
             [gmaps.location :as loc]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; GeoCoding
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Is this a good way to deal with singletons?
(def ^:private geocoder (atom nil))

(defn get-geocode
  [location]
  (compare-and-set! geocoder nil (google.maps.Geocoder.))
  (let [res (chan)]
    (.geocode @geocoder
              location
              (fn [result status]
                (put! res {:status status :result result})))
    res))

(defn get-location
  [site]
  (let [out (chan)]
    (go (let [sitename (:name site)
              geo (<! (get-geocode sitename))]
          (>! out {:sitename sitename 
                   :location (loc/geocode-to-location geo)})))
    out))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Directions Service
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private directions-service (atom nil))

#_(defn get-directions
  [opts]
  (compare-and-set! directions-service nil (google.maps.DirectionsService.))
  (let [out (chan)
        popts (assoc opts :origin (loc/lat-lng (:origin opts)))]
    (.route @directions-service (clj->js popts)
            (fn [res stat]
              (if (= stat "OK")
                (go (>! out res))
                (maybe-retry stat get-directions opts))))
    out))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Distance Matrix
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private distance-matrix-service (atom nil))

(defn get-distances
  [opts]
  (compare-and-set! distance-matrix-service nil
                    (google.maps.DistanceMatrixService.))
  (let [out  (chan)
        default {:travelMode google.maps.TravelMode.WALKING,
                 :unitSystem google.maps.UnitSystem.METRIC}]
    (.getDistanceMatrix @distance-matrix-service
                        (clj->js (merge default opts))
                        (fn [r s] (put! out {:status s :result r})))
    out))
