# paths

A routing library for Clojure/Ring.

paths is a data-structure based routing library. The goal is to provide an easy to extend routing library for web service development.

Please give feedback/suggestions/etc through github issues.



## Example

run `lein ring server` in `examples/paths_example`

This will launch a web server and open your browser at [http://localhost:3000](http://localhost:3000), showing you a series of links that should be self-explanatory.



## Installation

Add

[![Current Version](https://clojars.org/paths/latest-version.svg)](https://clojars.org/paths)

to your leiningen `:dependencies`.



## Usage

TLDR: require `[mx.paths.core :refer [pathsize]]`, declare your routes `(def routes ["/something" {:get #'somethings-handler}])`, implement the handler `(defn somethings-handler [arg1 arg2] {:body (str "look mah, no hands! and with " arg1 " and " arg2)})`, paths-ize your ring app `(def app (pathsize routes))` and you're ready to serve (`lein ring server-headless`) and visit http://localhost:3000/something?arg1=left&arg2=right !

Read on if you want to learn about the details.

### 1 Define your routes
**Route definition** consists of a vector that must have a pair of string followed by a map, i.e.

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
- In the route definition you state the route and the handlers for each HTTP method (usually `:get`/`:post`/`:put`/`:delete`). `paths` also accepts `:any` (which is only executed as a last alternative).

- You **must** pass the handlers in `var` form, i.e. using `#'`.

- For **route parameters**, use the traditional way of defining a route with an element that starts with a colon, e.g. `/this/path/accepts/:anything/there`. `paths` will put a parameter named `:anything` into the request's `:params` map. As you'll see in more detail in the next section, to access the route parameters, your handler simply declares that it expects a parameter with the same name (e.g. `(defn my-handler [anything] ...)`).

- For **wildcards**, use the keyword `:*` in your route, i.e. `"/i/accept/anything/:*" {:any #'my-fn}` All requests that get caught by this rule (e.g. `/i/accept/anything/123/456/0000`) will be served by the specified handler.

- For **resource and static file handling**, require `mx.paths.handlers`, add a wildcard route definition followed by `paths`'s `resource-handler` or `file-handler` helpers. E.g. `"/public/:*" resource-handler`, `"/download/:*" file-handler`. Resources are expected to be in your project's `/resources` folder. Static files should be placed under `/resources/public`.

- To define your own handling of 404 not found, add a `:404` route with an `:any`, e.g. `:404 {:any #'my-404-handler}`. Your 404 handler should take a single `request` parameter.

- Route definition does not support contextualized routes - routes **must be explicitely defined**! This means you need to specify the whole path to the route (i.e. you need to use `/user/sign/in`, `/user/sign/up`, `/user/sign/out` - there's no way to do `/user` and then have the sub-routes `/in`, `/up`, `/out` under the `/user` context). Sorry about that.



### 2 Implement your handlers
Handlers in `paths` are simple functions (i.e. defined through `defn`). They can have any number of arguments (0+).

Some examples of handler implementations:
```clojure
(require '[ring.util.response :refer [response]])

(defn my-handler1 [request]
  (response (str request)))

(defn my-handler2 [^{:name "request"} req]
  (response (str req)))

(defn my-handler3 [arg1 ^{:name "request"} req arg2]
  (response (str arg1 ", " arg2 ", " (keys req))))

(defn handler-with-args [p1 p2]
  (response (str p1 "\n" p2)))

(defn handler-with-no-args []
  (response "no args"))

(defn handler-wildcard [wildcards]
  (response wildcards))
```

- When the handler is being dispatched by `paths`, the arguments will be mapped from the request object's `:params` map.

- You can also send the whole request object into your handler by having a parameter named request.

- Say you want to have a parameter with a different name in your handler. You can rename it by adding the `:name` key as metadata. The value of this key is the name of the expected parameter in the request object. (e.g. `(defn my-handler [^{:name "name-in-request"} nir] ...)`

- The handlers _should_ return the same as any ring handler (a map). `ring.util.response` can be used to construct responses.



### 3. Setup routing with Ring
With the routes defined and the handlers implemented, all that is left to do is to wire things up with Ring.

`paths` depends on `ring.middleware.params` and `ring.middleware.keyword-params`. Assuming your app doesn't depend on these middleware, you can use `mx.paths.core/pathsize` to generate your app, e.g. `(def app (pathsize routes))` or `(def app (-> routes (pathsize) (...)))`. This will also automatically include `paths`' `wrap-route-params` middleware.

In case you're using other middleware that depend on the params middleware, you must include the aforementioned middleware and use `mx.paths.core/route` with your routes. E.g.

```clojure
(def app
  (->
    routes
    (route)
    (...) ; your middleware
    (wrap-keyword-params)
    (wrap-params)
    ))
```

And now just serve your app!



### 4. `paths` structure
`paths` supports a few more modes of operations. To explain that, we should look into how things are structured.

This is how `paths` works
![How paths works](/doc/how-paths-works.jpg?raw=true)

The source is the routes definition vector. It is transformed into a routes tree. This tree is queried every time a request comes in. A handler is matched through the request and then dispatched (i.e. executed) with the correct arguments.

#### `mx.paths.tree`
This module contains the functions that build a routes tree from a routes definition, namely `create-routes-tree`. There might be an ocasion where you'll want to use this function to create a routes tree and keep a reference to it.

#### `mx.paths.matcher`
This module contains the functions that, given a routes tree, match a request to a handler. `match` returns a vector where the first element is the matched handler (or nil if not found) and the second element is the route parameters (or nil if none exist).

#### `mx.paths.dispatcher`
This module contains the functions that dispatch the request. Given a routes tree and the request object, `dispatch` tries to find a handler in the tree.

If none is found, the 404 Not Found handler is executed (either the one present in the routes definition or the default one).

If a handler is found, parameters are merged into the request object's `:params` map, so that `handle`'s magic can be cast - i.e. the handler's metadata is inspected to determine what parameters it takes; the handler is called with the required arguments or nil if they are not found in the request object.

#### `mx.paths.core`
This module contains 3 different sets of functions.

`route` is a wrapper around `dispatch`. It is intended to be used in your Ring app definition. It is how `paths` executes and can take either a routes definition or a routes tree as an argument.

`query` is a wrapper around `match`. It is intended to be used in your Ring app definition. It allows you to integrate `paths` with other libraries (e.g. [enforcer](https://github.com/meta-x/enforcer)) or code that might need to consult the routes tree in runtime.

Even though `route` and `query` can both take either a routes definition or a routes tree as argument, the general use case is for you to build a routes tree beforehand with `mx.paths.tree/create-routes-tree` and pass it to these functions.

`pathsize` and `wrap-middleware` are app definition helpers. `pathsize` is useful when you don't have any other middleware that is dependent on `wrap-keyword-params` or `wrap-params`. `wrap-middleware` simply bundles these middleware for you.

#### `mx.paths.middleware`
This module contains a single `wrap-route-params`, the middleware function that takes route parameters and merges them into the request object's `:params` map.

#### `mx.paths.handlers`
This module contains helpers for resource and file handling as well as the default 404 handler.



## TODO / You can help by

- writing real tests

- improve doc

- ask questions, make suggestions, etc



## License

Copyright © 2014 Tony Tam

Released under the MIT license.
