(ns gmaps.core
  (:require [gmaps.location :as loc]
            [gmaps.map.directions :as dir]))

(def maps (atom {}))

(defn- init-args
  "Returns the sub-map of map-data that are required by the API to
  create a map."
  [map-data]
  (let [init-keys #{:center :zoom :disableDefaultUI :mapTypeId}
        default {:disableDefaultUI true
                 :zoom 12
                 :center nil
                 :mapTypeId google.maps.MapTypeId.ROADMAP}]
    (merge default
           (into {} (filter (fn [[k v]] (contains? init-keys k)) map-data)))))

(defn- update-map*
  [{:keys [map-obj map-data]} new-data]
  (when-not (= (-> map-data :directions) (-> new-data :directions))
    (dir/set-directions! map-obj (:directions new-data)))
  (when-not (loc/same-place? (.getCenter map-obj) (-> new-data :center))
    (.setCenter map-obj (-> new-data :center clj->js))))

(defn update-map!
  "Updates the map at elem in place to reflect the new data."
  [elem new-data]
  (when-let [{:keys [map-obj map-data] :as map} (get @maps elem)]
    (when (not= map-data new-data) 
      (update-map* map new-data))))

(defn attach-map!
  "Creates a new map object attached to the given DOM element if one does not
  already exist and updates it to match the given data. If there is already a
  map attached it is modified to show the new data." 
  [elem map-data]
  (if (not (contains? @maps elem))
    (let [map-obj (google.maps.Map. elem (clj->js (init-args map-data)))]
      (swap! maps assoc elem {:map-obj map-obj :map-data map-data})
      (update-map* {:map-obj map-obj :map-data map-data} map-data))
    (throw "Error! You are trying to attach a new map to a DOM element
    that already has one. Call detach-map! first, or use update-map!
    instead.")))

(defn detach-map!
  [elem]
  (if-let [{:keys [map-obj]} (get @maps elem)]
    (do 
      (swap! maps (fn [s] (dissoc s elem)))
      (dir/cleanup! map-obj))))
