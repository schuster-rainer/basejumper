;; Download sample database from http://www.medbase.ca/download/VFOXPRO9.0/PROGRAM%20FILES/MICROSOFT%20VISUAL%20FOXPRO%209/SAMPLES/NORTHWIND/

(ns db
  (:require [basejumper.core :as bj]
            [basejumper.cdx :as c]
            [clj-time.core :as t]))

(def filename "northwinddbf/CATEGORIES")
(def dbf (bj/load-dbf (str filename ".DBF")))
(def cdx (bj/load-cdx (str filename ".CDX")))

(-> cdx :header :sort-order)
(-> cdx :header :key-and-for-expression)

(:fields dbf)
(-> dbf :header)

;; error in serializing fields
(-> dbf :fields (get 2) :name)

;; doesn't work yet
(-> dbf :records)
