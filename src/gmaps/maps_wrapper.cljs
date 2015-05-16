(ns gmaps.maps-wrapper
  (:require [gmaps.maps-data :as md]))

(def maps (atom {}))

(def directions-renderers (atom {}))

(defn- get-directions-renderer!
  [map-obj]
  (if-let [dr (get @directions-renderers map-obj)]
    dr
    (let [dr (google.maps.DirectionsRenderer.)]
      ;; We can't set the map here since we need to set the map to null to
      ;; remove the directions...
      ; (.setMap dr map-obj)
      (swap! directions-renderers assoc map-obj dr)
      dr)))

(defn- delete-directions-renderer!
  [map-obj]
  (swap! directions-renderers dissoc map-obj))

(defn valid?
  [md]
  (and (-> md :opts :center :lat) (-> md :opts :center :lng)))

(defn default-map-opts []
  {:disableDefaultUI true
   :zoom 12
   :center nil
   :mapTypeId google.maps.MapTypeId.ROADMAP})

(defn create-map
  "Returns a hashmap representing a desired map display state.
  Basically just fills in a bunch of defaults..."
  [map-opts & extra-data]
  (let [opts (merge (default-map-opts) map-opts)
        {:keys [markers directions]} (first extra-data)]
    {:opts opts
     :markers (if (nil? markers) [] markers)
     :directions directions}))

(defn- update-map*
  "Updates a map in place to reflect the given data."
  [{:keys [map-obj map-data]} new-data]
  (when (not= (-> map-data :directions) (-> new-data :directions))
    (let [dr (get-directions-renderer! map-obj)]
      (if (nil? (:directions new-data))
        (.setMap dr nil)
        (do 
          (.setMap dr map-obj)
          (.setDirections dr (:directions new-data))))))
  (when-not (md/same-place (.getCenter map-obj) (-> new-data :opts :center))
    (.setCenter map-obj (-> new-data :opts :center clj->js))))

(defn update-map! [elem new-data]
  (when-let [{:keys [map-obj map-data] :as map} (get @maps elem)]
    (when (not= map-data new-data) 
      (assert (valid? new-data))
      (update-map* map new-data))))

(defn attach-map!
  "Creates a new map object attached to the given DOM element if one does not
  already exist and updates it to match the given data. If there is already a
  map attached it is modified to show the new data." 
  [elem map-data]
  (.log js/console (clj->js map-data))
  (when (valid? map-data) 
    (when (not (contains? @maps elem))
      (let [map-obj (google.maps.Map. elem (clj->js (:opts map-data)))]
        ;; (.setCenter map-obj (-> map-data :opts :center clj->js))
        (swap! maps assoc elem {:map-obj map-obj :map-data map-data})
        (update-map* {:map-obj map-obj :map-data map-data} map-data)))))

(defn detach-map!
  [elem]
  (if-let [{:keys [map-obj]} (get @maps elem)]
    (do 
      (swap! maps (fn [s] (dissoc s elem)))
      (delete-directions-renderer! map-obj))))
