(ns gmaps.map.markers
  (:require [clojure.set :as s]
            [gmaps.location :as loc]))

(def ^:private markers (atom {}))

(defn- create-marker
  [data]
  (google.maps.Marker.
   (clj->js data)))

;; TODO: How much memory do these guys take up? Do I need to care?
(defn- get-marker
  [data]
  (when-not (contains? @markers data)
    (swap! markers assoc data (create-marker data)))
  (get @markers data))

(defn- setify [coll]
  (if (set? coll)
    coll
    (into #{} coll)))

(defn update-markers! [map-obj old new]
  (when-not (= old new)
    (let [old (setify old)
          new (setify new)]
      (when-not (= old new)
        ;; Remove markers no longer present
        (doseq [md (s/difference old new)]
          (.setMap (get-marker md) nil))
        ;; Add new markers to map
        (doseq [md (s/difference new old)]
          (.setMap (get-marker md) map-obj))))))
