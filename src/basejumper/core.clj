;; http://www.clicketyclick.dk/databases/xbase/

(ns basejumper.core
  (:use [basejumper.dbf]
        [basejumper.ndx]
        [basejumper.cdx])
  (:require [org.clojars.smee.binary.core :as b]
            [clojure.java.io :as io]))


(defn load-dbf
   "xbase Reference: http://www.clicketyclick.dk/databases/xbase/format/dbf.html
              _______________________  _______
    00h /   0| Version number      *1|  ^
             |-----------------------|  |
    01h /   1| Date of last update   |  |
    02h /   2|      YYMMDD        *21|  |
    03h /   3|                    *14|  |
             |-----------------------|  |
    04h /   4| Number of records     | Record
    05h /   5| in data file          | header
    06h /   6| ( 32 bits )        *14|  |
    07h /   7|                       |  |
             |-----------------------|  |
    08h /   8| Length of header   *14|  |
    09h /   9| structure ( 16 bits ) |  |
             |-----------------------|  |
    0Ah /  10| Length of each record |  |
    0Bh /  11| ( 16 bits )     *2 *14|  |
             |-----------------------|  |
    0Ch /  12| ( Reserved )        *3|  |
    0Dh /  13|                       |  |
             |-----------------------|  |
    0Eh /  14| Incomplete transac.*12|  |
             |-----------------------|  |
    0Fh /  15| Encryption flag    *13|  |
             |-----------------------|  |
    10h /  16| Free record thread    |  |
    11h /  17| (reserved for LAN     |  |
    12h /  18|  only )               |  |
    13h /  19|                       |  |
             |-----------------------|  |
    14h /  20| ( Reserved for        |  |            _        |=======================| ______
             |   multi-user dBASE )  |  |           / 00h /  0| Field name in ASCII   |  ^
             : ( dBASE III+ - )      :  |          /          : (terminated by 00h)   :  |
             :                       :  |         |           |                       |  |
    1Bh /  27|                       |  |         |   0Ah / 10|                       |  |
             |-----------------------|  |         |           |-----------------------| For
    1Ch /  28| MDX flag (dBASE IV)*14|  |         |   0Bh / 11| Field type (ASCII) *20| each
             |-----------------------|  |         |           |-----------------------| field
    1Dh /  29| Language driver     *5|  |        /    0Ch / 12| Field data address    |  |
             |-----------------------|  |       /             |                     *6|  |
    1Eh /  30| ( Reserved )          |  |      /              | (in memory !!!)       |  |
    1Fh /  31|                     *3|  |     /       0Fh / 15| (dBASE III+)          |  |
             |=======================|__|____/                |-----------------------|  | <-
    20h /  32|                       |  |  ^          10h / 16| Field length       *22|  |   |
             |- - - - - - - - - - - -|  |  |                  |-----------------------|  |   | *7
             |                    *19|  |  |          11h / 17| Decimal count      *23|  |   |
             |- - - - - - - - - - - -|  |  Field              |-----------------------|  | <-
             |                       |  | Descriptor  12h / 18| ( Reserved for        |  |
             :. . . . . . . . . . . .:  |  |array     13h / 19|   multi-user dBASE)*18|  |
             :                       :  |  |                  |-----------------------|  |
          n  |                       |__|__v_         14h / 20| Work area ID       *16|  |
             |-----------------------|  |   \\                |-----------------------|  |
          n+1| Terminator (0Dh)      |  |    \\       15h / 21| ( Reserved for        |  |
             |=======================|  |     \\      16h / 22|   multi-user dBASE )  |  |
          m  | Database Container    |  |      \\             |-----------------------|  |
             :                    *15:  |       \\    17h / 23| Flag for SET FIELDS   |  |
             :                       :  |         |           |-----------------------|  |
        / m+263                      |  |         |   18h / 24| ( Reserved )          |  |
             |=======================|__v_ ___    |           :                       :  |
             :                       :    ^       |           :                       :  |
             :                       :    |       |           :                       :  |
             :                       :    |       |   1Eh / 30|                       |  |
             | Record structure      |    |       |           |-----------------------|  |
             |                       |    |       \\  1Fh / 31| Index field flag    *8|  |
             |                       |    |        \\_        |=======================| _v_____
             |                       | Records
             |-----------------------|    |
             |                       |    |          _        |=======================| _______
             |                       |    |         / 00h /  0| Record deleted flag *9|  ^
             |                       |    |        /          |-----------------------|  |
             |                       |    |       /           | Data               *10|  One
             |                       |    |      /            : (ASCII)            *17: record
             |                       |____|_____/             |                       |  |
             :                       :    |                   |                       | _v_____
             :                       :____|_____              |=======================|
             :                       :    |
             |                       |    |
             |                       |    |
             |                       |    |
             |                       |    |
             |                       |    |
             |=======================|    |
             |__End_of_File__________| ___v____  End of file ( 1Ah )  *11"
  [filename]
  (b/decode dbf-codec (io/input-stream filename)))

(defn load-ndx
  "    _______________________  _______
 0 | Starting page no      |  ^
 1 |                     *1|  |
 2 |                       |  |
 3 |                       |  |
   |-----------------------|  |
 4 | Total no of pages   *2| File
 5 |                       | header
 6 |                       |  |
 7 |                       | (page 0)
   |-----------------------|  |
 8 | (Reserved)            |  |
 9 |                       |  |
10 |                       |  |
11 |                       |  |
   |-----------------------|  |
12 | Key length            |  |
13 |                       |  |
   |-----------------------|  |
14 | No of keys per page   |  |
15 |                       |  |
   |-----------------------|  |
16 | Key type: 0 = char    |  |
17 |           1 = Num     |  |
   |-----------------------|  |
18 | Size of key record  *3|  |
19 |                       |  |
20 |                       |  |
21 |                       |  |
   |-----------------------|  |
22 | (Reserved)            |  |
   |-----------------------|  |
23 | Unique flag         *4|  |
   |-----------------------|  |
24 | String defining the   |  |
   | key                   |  |
   :                       :  |
   :                       :  |
   :                       :  |
511|                       |  |
   |=======================| _v____            Link to lover level
  0| No of valid entries   |  ^           __  |=======================|
  1| on current page     *5|  |          /   0| Pointer to lower level|
  2|                       |  |         /    1| (next page)           |
  3|                       |  |        /     2|                       |
   |-----------------------|  |       /      3|                       |
  4|                       |  |      /        |-----------------------|
   | Array of key entries  | _|_____/        4| Record number in      |
   |                     *6|  |              5| data file             |
   |                       | Page            6|                       |
   |                       |  |              7|                       |
   |                       |  |               |-----------------------|
   |                       | _|_____         8| Key data            *7|
   :.......................:  |    \\         :                       :
   :.......................:  |     \\       N|                       |
   :.......................:  |      \\_____  |=======================|
511|                       |  |
   |=======================| _v_____           Link to DBF
  0| No of valid entries   |  ^           __  |=======================|
  1| on current page     *5|  |          /   0| No of keys on page    |
  2|                       |  |         /    1|                       |
  3|                       |  |        /     2|                       |
   |-----------------------|  |       /      3|                       |
  4|                       |  |      /        |-----------------------|
   | Array of key entries  |  |     /        4| Left page pointer     |
   |                     *6|  |              5|                       |
   |                       |__|___/          6|                       |
   |                       |  |              7|                       |
   |                       |__|___            |-----------------------|
   :.......................:  |  \\          8| DBF record num      *8|
   |                       |  |   \\          :                       :
   :.......................:  |    \\       11|                       |
   |                       |  |     \\        |-----------------------|
   :.......................:  |      \\      8| Key data              |
   |                       |  |       \\      :                       :
   :.......................:  |        \\    N|                       |
511|                       |  |         \\__  |=======================|
   |=======================| _v_____
  "
  [filename]
  (b/decode ndx-codec (io/input-stream filename)))


(defn load-cdx
  "    _______________________  _______
  0 | Pointer to root node  |  ^
  1 |                       |  |
  2 |                       |  |
  3 |                       |  |
    |-----------------------|  |
  4 | Pointer to free list  | File
  5 | (-1 if empty)         | header
  6 |                       |  |
  7 |                  *8   | (page 0)
    |-----------------------|  |
  8 | Version no.      *10  |  |
  9 |                       |  |
 10 |                       |  |
 11 |                       |  |
    |-----------------------|  |
 12 | Key length            |  |
 13 |                  *9   |  |
    |-----------------------|  |
 14 | Index options *1      |  |
    |-----------------------|  |
 15 | Index Signature       |  |
    |-----------------------|  |
 16 | (Reserved)            |  |
 17 |                       |  |
    : (Currently all NULL's):  |
    :                       :  |
    :                       :  |
 501|                       |  |
    |-----------------------|  |
 502| Sort order *2         |  |
 503|                       |  |
    |-----------------------|  |
 504| Total expression      |  |
 505| length (FoxPro 2)     |  |
    |-----------------------|  |
 506| FOR expression length |  |
 507| (binary)              |  |
    |-----------------------|  |
 508| (Reserved)            |  |
 509|                       |  |
    |-----------------------|  |
 510| Key expression length |  |
 511| (binary)              |  |
    |=======================| _v____
 512| Key & FOR expression  |  ^
 513|            *3         |  |
    :                       :  |
    :                       :  |
1023|                       |  |
    |=======================| _v____
  0 | Node attributes *4    |  ^
  1 |                       |  |
    |-----------------------|  |
  2 | Number of keys        |  |
  3 |                       |  |
    |-----------------------| Non
  4 | Pointer to left       | leaf
  5 | brother node          | page
  6 | (-1 if no left node)  |  |
  7 |                       | (compressed)
    |-----------------------|  |
  8 | Pointer to right      |  |
  9 | brother node          |  |
 10 | (-1 if no right node) |  |
 11 |                       |  |
    |-----------------------|  |
 12 |                       |  |
    |                       |  |           __  |=======================|
    |                       |  |          /    | Key data              |
    |                       | NON        /     :                       :
    |                       | leaf      /      :                       :
    |                       | page     /       |                       |
    |                       |  |      /        |-----------------------|
    | Array of key entries  | _|_____/        M| Record number in      |
    |                       |  |               | data file             |
    |                       |  |               | (high order byte      |
    |                       |  |              N|  first)               |
    |                       |  |               |-----------------------|
    |                       | _|_____         m| Pointer to child page |
    :.......................:  |    \\         |                       |
    :.......................:  |     \\        |                       |
    :.......................:  |      \\      n|                       |
 511|                       |  |       \\____  |=======================|
    |=======================| _v_____
  0 | Node attributes *4    |  ^
  1 |                       |  |
    |-----------------------|  |
  2 | Number of keys        |  |
  3 |                       |  |
    |-----------------------|  |
  4 | Pointer to left       | Leaf
  5 | brother node          | page
  6 | (-1 if no left node)  |  |
  7 |                       | (compressed)
    |-----------------------|  |
  8 | Pointer to right      |  |
  9 | brother node          |  |
 10 | (-1 if no right node) |  |
 11 |                       |  |
    |-----------------------|  |
 12 | Free space available  |  |
 13 | in page               |  |
    |-----------------------|  |
 14 | Record number mask    |  |
 15 |                       |  |
 16 |                       |  |
 17 |                       |  |
    |-----------------------|  |
 18 | Duplicate count mask  |  |  *11
    |-----------------------|  |
 19 | Trailing byte count mask |  *11
    |-----------------------|  |
 20 |*5 record no           |  |
    |-----------------------|  |
 21 |*5 duplicate count     |  |
    |-----------------------|  ^           __  |=======================|
 22 |*5 trailing count      |  |          /   0| Recno/supCount/       |
    |-----------------------| Leaf       /    1| TrailCount            |
 23 |*6 holding record no   | page      /     2| *7)                   |
    |-----------------------|  |       /      3|                       |
 24 |                       |  |      /        |-----------------------|
    | Array of key entries  | _|_____/        4| Record number in      |
    |                       |  |              5| data file             |
    |                       |  |              6|                       |
    |                       |  |              7|                       |
    |                       |  |               |-----------------------|
    |                       | _|_____         8| Key data              |
    :.......................:  |    \\         :                       :
    :.......................:  |     \\       N|                       |
 511|                       |  |      \\_____  |=======================|
    |=======================| _v_____
  "
  [filename]
  (b/decode cdx-codec (io/input-stream filename)))

