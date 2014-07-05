;; http://www.clicketyclick.dk/databases/xbase/format/dbf.html

(ns basejumper.dbf
  (:require
   [org.clojars.smee.binary.core :as b]
   [clj-time.core :as t])
  (:import org.clojars.smee.binary.core.BinaryIO))

(def db-type-mask  {0x02 :foxbase
                    0x03 :without-dbt
                    0x04 :dbase-4-without-memo
                    0x05 :dbase-5-without-memo
                    0x07 :dbase-3-without-memo-visual-objects
                    0x30 :visual-fox-pro
                    ;0x30 :visual-fox-pro-with-dbc
                    0x31 :visual-fox-pro-autoincrement
                    0x43 :dbv-memo-var-size
                    0x7b :dbase-4-with-memo
                    0x83 :file-with-dbt
                    ;0x83 :dbase-3+-with-memo
                    0x87 :dbase-3-with-memo-visual-objects-clipper
                    0x8b :dbase-4-with-memo
                    0x8e :dbase-4-with-sql-table
                    0xb3 :dbv-and-dbt-memo
                    0xe5 :clipper-6-with-smt-memo
                    0xf5 :foxpro-with-memo
                    0xfb :foxpro})

(def modified-date
  "3 bytes YYMMDD convert to DateTime"
  (reify b/BinaryIO
    (read-data [this big-in little-in]
               (let [{year :year month :month day :day}
                     (b/read-data (b/ordered-map
                                   :year :ubyte
                                   :month :ubyte
                                   :day :ubyte) big-in little-in)]
                 (if (> year 70)
                   (t/date-time (+ 1900 year) month day)
                   (t/date-time (+ 2000 year) month day))))
    (write-data [this big-in little-in value]
                (throw (ex-info "not implemented" {:codec this :value value})))))

(def dbf-header
  (b/ordered-map
   :type :ubyte ; 0 - db-type-mask
   :modified modified-date ; 1-3
;;    :modified (union 3 {:bytes (ordered-map
;;                                :year :ubyte
;;                                :month :ubyte
;;                                :day :ubyte )})
   :record-count :uint-le ; 4-7
   :header-size :ushort-le ; 8-9
   :record-size :ushort-le ; 10-11
   :reserved-1 (b/repeated :ubyte :length 2) ; 12-13
   :incomplete-trans :ubyte ; 14
   :encryption :ubyte ; 15
   :free-record-thread (b/repeated :ubyte :length 4) ; 16-19
   :multi-user (b/repeated :ubyte :length 8) ; 20-27
   :index? :ubyte ; 28
   :lang-id :ubyte ; 29
   :reserved-2 (b/repeated :ubyte :length 2))) ; 30-31

(def dbf-field-descriptor
  (b/ordered-map
   :name (b/string "ASCII" :length 11) ; 0-10
   :type :ubyte ; 11
   :offset :uint-le ; 12-15
   :size :ubyte ; 16 - Max Val 255
   :decimal-count :ubyte ; 17 - Val <= 15
   :reserved-1 (b/repeated :ubyte :length 2) ; 18-19
   :work-area-id :ubyte ; 20
   :reserved-2 (b/repeated :ubyte :length 2) ; 21-22
   :fields? :ubyte ; 23
   :reserved-2 (b/repeated :ubyte :length 7) ; 24-30
   :index? :ubyte)) ; 31

(def field-descriptor
  (reify b/BinaryIO
    (read-data [this big-in little-in]
               (let [probe (b/read-data :byte big-in little-in)
                     separator? (= probe 0x0d)]
                 (if (separator?)
                   0x0d
                   probe)))
    (write-data [this big-in little-in value]
                (throw (ex-info "not implemented" {:codec this :value value})))))

(def dbc (b/blob :length 263))

(def dbf-record
  (b/ordered-map
   :deleted? (b/enum :ubyte {:false 0x20
                             :true 0x2a})))

;; (def dbf-codec
;;   (b/ordered-map
;;    :header (b/header dbf-header
;;                      #(b/ordered-map
;;                        :fields (b/repeated dbf-field-descriptor :separator 0x0d)
;;                        :records (b/repeated :ubyte %))
;;                      #(% :header-size)
;;                      :keep-header true)))

(def dbf-codec
  "TODO: fix field separator"
  (b/ordered-map
   :header dbf-header
   :fields (b/repeated dbf-field-descriptor :separator \return)))
