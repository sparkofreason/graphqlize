(ns graphqlize.lacinia.type)

(defn lacinia-type [heql-attr-type entity-ident-in-pascal-case]
  (case heql-attr-type
    :attr.type/ref entity-ident-in-pascal-case
    (:attr.type/big-integer :attr.type/integer) 'Int
    :attr.type/long 'Long
    (:attr.type/float :attr.type/double) 'Float
    :attr.type/boolean 'Boolean
    :attr.type/string 'String
    :attr.type/uuid 'UUID
    :attr.type/date-time 'DateTime
    :attr.type/date-time-with-time-zone 'DateTimeWithTimeZone
    :attr.type/decimal 'BigDecimal
    (:attr.type/date :attr.type/time :attr.type/time-span
     :attr.type/offset-time
     :attr.type/ip-address :attr.type/json :attr.type/unknown) 'String
    nil))