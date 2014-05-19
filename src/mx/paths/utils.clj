(ns mx.paths.utils)

(defn combine [& maps]
    (apply merge-with combine maps))

(defn deep-merge [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))


