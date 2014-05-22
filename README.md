# paths

(Enhanced) routing library for Clojure/Ring.

paths is a data-structure based routing library. The goal is to provide an easy to extend routing library for web service development.

ATTN: under development!

Please give feedback/suggestions/etc through github issues.



## Example

run `lein ring server` in `examples/paths_example`

This will launch a web server and open your browser at (http://localhost:3000)[http://localhost:3000], showing you a series of links that should be self-explanatory.



## Installation

Add

```clojure
[paths "0.1.0-beta1"]
```

to your leiningen `:dependencies`.



## Usage

### 1. Require it
`paths` expects `ring.middleware.params` and `ring.middleware.keyword-params` to be used as middleware, so be sure to include it. ; TODO: fix this by including these middlewares into path

```clojure
(:require [mx.paths.core :refer [router]]
          [ring.middleware.params :refer [wrap-params]]
          [ring.middleware.keyword-params :refer [wrap-keyword-params]])
```



### 2.1 Define your routes
- Route definition consists of a vector that must have a pair of a string followed by a map, i.e. `("/path" {:get #'handler$get})+` for you BNF junkies

- In the route definition you only state the paths and the handlers for each method (usually :get/:post/:put/:delete)

- Since the 2nd element is a map, it should be easy to extend `paths` i.e. you can add (almost) anything to the the map and (TBI) have it passed to your handler or executed by the routing library

- You **must** pass the handlers in `var` form, i.e. using `#'`

- Route definition does not support contextualized routes - they **must be explicitely defined**! Sorry about that

- For wildcards/route parameters, use the traditional way of defining a path like `/this/path/accepts/:anything/there`. `paths` will put a parameter named `:anything` in the request's `:params` map which you may optionally obtain by declaring it in the handler's argument list

Example routes definition:
```clojure
(def routes [
  "/" {:get #'index}
  "/basic" {:get #'basic-handler}
  "/with/args" {:get #'handler-with-args}
  "/with/no/args" {:get #'handler-with-no-args}
  "/this/accepts/:wildcards/yeah" {:get #'handler-wildcard}
  "/this/is/an/endpoint" {:get #'handler-get :delete #'handler-delete :post #'handler-post :put #'handler-put}
])
```

### 2.2 Implement your handlers
Handlers in `paths` are functions defined through `defn`. They can have any number of arguments (0+). The arguments will be mapped from the request's `:params`. There's a special case where if there is only 1 parameter and it is named `request`, `paths` will send the request object.

The handlers _should_ return the same as any ring handler (a map). `ring.util.response` can be used to construct responses.

Some examples of handler implementations:
```clojure
(require '[ring.util.response :refer [response]])

(defn basic-handler [request]
  (response (str request)))

(defn handler-with-args [p1 p2]
  (response (str p1 "\n" p2)))

(defn handler-with-no-args []
  (response "no args"))

(defn handler-wildcard [wildcards]
  (response wildcards))
```



### 3. Setup routing with Ring
Apply the `router` function to your routes definition.

Ideally you'll use `router` in the ring app definition.

`router` is the function that "compiles" the routes definition to a ring handler function that will route the requests to your defined handler. `router` also takes an optional 404 handler function as its 2nd argument. The 404 function must take one single argument, the http request. If no 404 handler is provided, a default 404 response will be generated.

You should use `router` in your ring app definition (with your desired middlewares).

```clojure
(def app
  (->
    (router routes)
    (wrap-keyword-params)
    (wrap-params)
    ))
```



## TODO / You can help by

- writing real tests

- improve doc

- etc



## License

Copyright © 2014 Tony Tam

Released under the MIT license.
