(ns graphqlize.lacinia.eql
  (:require [inflections.core :as inf]
            [clojure.string :as c-str]
            [honeyeql.debug :refer [trace>>]]))

(defn- eql-root-attr-ns [namespaces selection-tree]
  (let [raw-namespaces   (map name namespaces)
        raw-entity-ident (-> (ffirst selection-tree)
                             namespace
                             inf/hyphenate)]
    (if-let [raw-ns (first (filter #(c-str/starts-with? raw-entity-ident %) raw-namespaces))]
      (let [x (c-str/replace-first raw-entity-ident (str raw-ns "-") "")]
        (if (= x raw-ns)
          x
          (str raw-ns "." x)))
      raw-entity-ident)))

#_(eql-root-attr-ns [:public] {:Language/languageId [nil]})
#_(eql-root-attr-ns [:person] {:PersonStateProvince/languageId [nil]})

(def ^:private reserved-args #{:first :offset})

(defn- ident [root-attr-ns args]
  (->> (remove (fn [[k _]]
                 (reserved-args k)) args)
       (mapcat (fn [[k v]]
                 [(keyword root-attr-ns (inf/hyphenate (name k))) v]))
       vec))

#_(ident "language" {:first 1})
#_(ident "language" {:language-id 1})
#_(ident "film-actor" {:film-id 1 :actor-id 1})

(defn- to-eql-param [[arg value]]
  (case arg
    :first [:limit value]
    [arg value]))

(defn- parameters [args]
  (->> (filter (fn [[k _]]
                 (reserved-args k)) args)
       (map to-eql-param)
       (into {})))

#_(parameters {:first 10 :offset 10})

(declare properties)

(defn- field->prop [namespaces selection-tree field]
  (let [root-attr-ns (eql-root-attr-ns namespaces selection-tree)
        prop         (->> (name field)
                          inf/hyphenate
                          (keyword root-attr-ns))]
    (if-let [sub-selection (first (selection-tree field))]
      (let [sub-selection-tree (:selections sub-selection)]
        {prop (properties namespaces sub-selection-tree)})
      prop)))

(defn- properties [namespaces selection-tree]
  (vec (map #(field->prop namespaces selection-tree %) (keys selection-tree))))

(defn generate [namespaces selection-tree args]
  (let [root-attr-ns (eql-root-attr-ns namespaces selection-tree)
        ident        (ident root-attr-ns args)
        parameters   (parameters args)
        ident        (if (empty? parameters)
                       ident
                       (list ident parameters))
        properties   (properties namespaces selection-tree)
        eql          [{ident properties}]]
    (trace>> :eql eql)))