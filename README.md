# paths

(Enhanced) routing library for Clojure/Ring.

paths is a data-structure based routing library. The goal is to provide an easy to extend routing library for web service development.

ATTN: under development!

Please give feedback/suggestions/etc through github issues.



## Example

run `lein ring server` in `examples/paths_example`

This will launch a web server and open your browser at [http://localhost:3000](http://localhost:3000), showing you a series of links that should be self-explanatory.



## Installation

Add

```clojure
[paths "0.1.0-beta2"]
```

to your leiningen `:dependencies`.



## Usage

### 1. Require it
`paths` expects `ring.middleware.params` and `ring.middleware.keyword-params` to be used as middleware, so be sure to include it. ; TODO: fix this by including these middlewares into paths

```clojure
(:require [mx.paths.core :refer [router-with-def]]
          [ring.middleware.params :refer [wrap-params]]
          [ring.middleware.keyword-params :refer [wrap-keyword-params]])
```



### 2.1 Define your routes
- Route definition consists of a vector that must have a pair of string followed by a map, i.e. `"/path" {:get #'handler$get}`.

- In the route definition you only state the paths and the handlers for each method (usually :get/:post/:put/:delete).

- Since the 2nd element is a map, it should be easy to extend `paths` i.e. you can add (almost) anything to the the map and (TODO) have it passed to your handler or executed by the routing library.

- You **must** pass the handlers in `var` form, i.e. using `#'`,

- Route definition does not support contextualized routes - they **must be explicitely defined**! This means you need to specify the whole path for routes (i.e. you need to use `/user/sign/in`, `/user/sign/up`, `/user/sign/out` - there's no way to do `/user` and then have the sub-routes `/in`, `/up`, `/out` under the `/user` context). Sorry about that.

- For wildcards and route parameters, use the traditional way of defining a path like `/this/path/accepts/:anything/there`. `paths` will put a parameter named `:anything` in the request's `:params` map - which you may optionally obtain by declaring it in the handler's argument list.

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
Finally, you need to apply one of the router functions to your routes definition - `(router-with-def routes-def)` or `(router-with-tree routes-tree)` - and use it in your ring app definition.

`router-with-def` is a function that takes a routes definition and returns a dispatcher function that will route the requests to your defined handler.

`router-with-tree` takes a previously compiled routes tree and returns the dispatcher function that will route the requests. You can create a routes-tree using `create-routes-tree`. These functions, together with `route` allow you to query `paths` to determine what's the handler for a given path.

They also take an optional 404 handler function as an argument. The 404 function must take one single argument, the http request. If no 404 handler is provided, a default 404 response will be generated.

Here, see if this decomplicates my explanation (this is how `paths` works):
![How paths works](/doc/how-paths-works.jpg?raw=true)

You should use the router functions in your ring app definition (with your desired middlewares).
There's also a `wrap-route-params` middleware in `mx.paths.middleware` that puts the route parameters into the request's `:params` map.

```clojure
(def app
  (->
    (router-with-def routes)
    (wrap-keyword-params)
    (wrap-params)
    ))
```



## TODO / You can help by

- writing real tests

- improve doc

- ask questions, make suggestions, etc



## License

Copyright © 2014 Tony Tam

Released under the MIT license.
