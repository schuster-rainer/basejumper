# basejumper

A Clojure library designed to read xBase files. It is a WORK IN PROGRESS and early alpha stage. Why I'm creating it? Why not?
Learning how to use org.clojars.smee/binary and I used to work on a xBase c++ library over 10 years ago.

## Usage

Download sample database from http://www.medbase.ca/download/VFOXPRO9.0/PROGRAM%20FILES/MICROSOFT%20VISUAL%20FOXPRO%209/SAMPLES/NORTHWIND/

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


## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
