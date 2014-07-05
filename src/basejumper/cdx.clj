;; http://www.clicketyclick.dk/databases/xbase/format/cdx.html

(ns basejumper.cdx
  (:require
   [org.clojars.smee.binary.core :as b])
  (:import org.clojars.smee.binary.core.BinaryIO))

(def index-options (b/enum :ubyte  ; 14 - Index options represented as the sum of the following values:
                        {:unique-index (unchecked-byte 1) ; 01 Unique index
                         :for-clause (unchecked-byte 8) ; 08 FOR clause
                         :bit-vector (unchecked-byte 16) ; 16 (10h) Bit vector (SoftC)
                         :compact-index (unchecked-byte 32) ; 32 (20h) Compact index format (FoxPro)
                         :compound-index (unchecked-byte 64) ; 64 (40h) Compounding index header (FoxPro)
                         :structure-index (unchecked-byte 128)})) ; 128 (80h) Structure index (FoxPro))

(def non-leaf-page
  "compressed non leaf page"
  (b/ordered-map
   :type :ushort-le ; 0-1 Node attributes represented as the sum of the following values:
                                                        ;; 0 Interior node (branch)
                                                        ;; 1 Root page
                                                        ;; 2 Leaf page
   :key-count :ushort-le ; 2-3
   :left-node :uint-le ; 4-7 - Pointer to left brother node (-1 if no left node)
   :right-node :uint-le ; 8-11 - Pointer to right brother node (-1 if no left node)
   :key-entries (b/repeated :ubyte :length 500)))

(def leaf-page
  "compressed leaf page.

  Key entries:
  At the start of this area, the recno / duplicate count / trailing count
  is stored (bit compressed). Each entry requires the number of bytes as
  indicated by :total-byte-count (byte 23). The key values are placed at the
  end of this area (working backwards) and are stored by eliminating any
  duplicates with the previous key and any trailing blanks.

 |=======================|
0| Recno/dupCount/       |
1| TrailCount            |
2| *7)                   |
3|                       |
 |-----------------------|
4| Record number in      |
5| data file             |
6|                       |
7|                       |
 |-----------------------|
8| Key data              |
 :                       :
N|                       |
 |=======================|
  "
  (b/ordered-map
   :type :ushort-le
   :key-count :ushort-le
   :left-node :uint-le
   :right-node :uint-le
   :free-space :ushort-le
   :record-number-mask :uint-le
   :duplicate-count-mask :ubyte
   :trailing-byte-count-mask :ubyte
   :record-bits :ubyte; 20 - Number of bits for record number
   :duplicate-bits :ubyte ; 021 - Number of bits for duplicate count
   :trailing-bits :ubyte ; Byte 022: Number of bits for trailing count
   :total-byte-count :ubyte; Byte 023: Number of bytes holding record number, duplicate count & trailing count (i.e. the total size of values in byte 20 - 22).
   :key-entries (b/string "ASCII" :length 488)))

(def cdx-header
  "A CDX is a compact IDX. The initial IDX contains one key per tag.
   The key is 10 byte character string which is the tag name.
   The record number stored with the key is the offset to the root
   page for that tag."
  (b/ordered-map
   :root-node-pointer :uint-le ; 0-3
   :free-list :uint-le ; 4-7 - -1 if none (FoxPro)
                       ;        0 if none (FoxBase)
   :version :uint-le ; 8-11 (Foxbase, FoxPro 1.x) No. of pages in file. (FoxPro 2.x) Reserved.
   :key-lenght :ushort-le ; 12-13 - Number and date keys are 8 bits long .
                                  ; Character keys are <= 100 bytes long.
                                  ; Note! Character keys are NOT terminated with 00h
   :index-options :ubyte
   :index-signature :ubyte ; 15
   :reserved-1 (b/repeated :ubyte :length 486) ; 16-501 (Currently all NULL's)
   :sort-order (b/enum :ushort-le {:ascending 0
                                 :descending 1}) ; 502-503
   :total-expression-length :ushort-le ; 504-505 (FoxPro 2)
   :for-expression-length :ushort-le ; 506-507 (binary)
   :reserved-2 (b/repeated :ubyte :length 2) ; 508-509
   :key-expression-length :ushort-le ; 510-511(binary)
   :key-and-for-expression (b/repeated :ubyte :length 512) ; Key first with null terminator, then FOR expression.
   ))

(def cdx-codec
  (b/ordered-map
   :header cdx-header))
