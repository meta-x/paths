(ns mx.paths.utils)

(defn combine
  [& maps]
  (apply merge-with combine maps))
