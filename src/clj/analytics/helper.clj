(ns analytics.helper)

(defmacro functionize  [macro]
  `(fn  [& args#]  (eval  (cons '~macro args#))))

(defmacro apply-macro  [macro args]
  `(apply  (functionize ~macro) ~args))
