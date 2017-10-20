(ns clj-momo.lib.es.pagination
  (:require [schema.core :as s]))

(def default-limit 100)

(defn list-response-schema [Model]
  "generate a list response schema for a model"
  {:data [Model]
   :paging {s/Any s/Any}})

(defn response
  "Make a paginated response adding summary info as metas"
  [results
   offset
   limit
   sort
   search_after
   hits]
  (let [offset (or offset 0)
        limit (or limit default-limit)
        previous-offset (- offset limit)
        next-offset (+ offset limit)
        previous? (and (not search_after)
                       (pos? offset))
        next? (if search_after
                (= limit (count results))
                (> hits next-offset))
        previous {:previous {:limit limit
                             :offset (if (> previous-offset 0)
                                       previous-offset 0)}}
        next {:next {:limit limit
                     :offset next-offset}}]
    {:data results
     :paging (merge
              {:total-hits hits}
              (when previous? previous)
              (when next? next)
              (when sort {:sort sort}))}))

(defn paginate
  [data {:keys [sort_by sort_order offset limit]
         :or {sort_by :id
              sort_order :asc
              offset 0
              limit default-limit}}]
  (as-> data $
    (sort-by sort_by $)
    (if (= :desc sort_order)
      (reverse $) $)
    (drop offset $)
    (take limit $)))
