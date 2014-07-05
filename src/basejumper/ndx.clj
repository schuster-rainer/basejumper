;; http://www.clicketyclick.dk/databases/xbase/format/ndx.html
(ns basejumper.ndx
  (:require
   [org.clojars.smee.binary.core :as b]
   [clj-time.core :as t])
  (:import org.clojars.smee.binary.core.BinaryIO))

(def ndx-root-node
  (b/ordered-map
   :starting-page :uint-le; 0-3 - offset is: page number x 512 bytes
   :total-pages :uint-le; 4-7 - Max = 7FFFFFh = 8,388,607
   :reserved-1 (b/repeated :ubyte :lenght 4); 8-11
   :key-lenght :ushort-le ; 12-13
   :keys-per-page :ushort-le ; 14-15
   :key-type (b/enum :ushort-le {:char 0
                               :num 1}) ; 16-17
   :key-record-size :uint-le; 18-21 - is a multiplum of 4. Record size is 4 (Pointer to next page)
                                    ; + 4 (record number i dbf) + key size ( as a multiplum of 4 ).
                                    ; i.e. if the key size is 10, the record size is 20 (4+4+12)
   :reserved-2 :ubyte ; 22
   :key-name (b/string "ASCII" :length 488) ; 24-511
   ))

(def ndx-codec
  (b/ordered-map
   :root-node ndx-root-node))
