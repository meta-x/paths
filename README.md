# paths

(Enhanced) routing library for Clojure/Ring.

paths is a data-structure based routing library. The goal is to provide an easy to extend routing library for web service development.

paths is different from other routing libraries such as Compojure ........



ATTN: under development!

Please give feedback/suggestions/etc through github issues.



## Example

run `lein ring server` in `examples/paths_example`

This will launch a webserver and open your browser in http://localhost:3000, showing you a series of links that should be self-explanatory.



## Installation

Add

```clojure
[paths "0.1.0-beta1"]
```

to your leiningen `:dependencies`.



## Usage

### 1. Require it
`paths` depends on `ring.middleware.params`, so be sure to include it.

```clojure
(:require [mx.paths.core :refer [router]])
```



### 2.1 Define your routes
- route definition consists of a vector that must have a pair that consists of a string followed by a map, i.e. ("/path" {:a-map #_})+ for you BNF junkies

- in the route definition you only state the paths and the handlers for each (usually :get/:post/:put/:delete)

- because the 2nd element is a map, it allows for easy extensibility i.e. you can add (almost) anything to the the map and (tbi) have it passed to you in your handler or executed by the routing library

- you must pass the handlers in `var` form, i.e. using `#'`

- for wildcards/route parameters, use the traditional way of defining a path `/this/path/accepts/:anything/there`. `paths` will put a parameter named `:anything` in the request's :params map which you may optionally use by declaring it in the handler's argument list.


```clojure
(def routes [

])
```

### 2.2 Implement your handlers
Handlers in paths are functions defined through `defn`. They can have any number of arguments (0+). The arguments will be mapped from the http request's `:params`. There's a special case where if there is only 1 parameter and it is named `request`, paths will send the http request object.

The handlers _should_ return the same as any ring handler (a map). `ring.util.response` can be used to construct responses.

```clojure

```



### 3.
Apply the `router` function to your routes definition. Use the paths router in the ring app definition.

`router` is the function that "compiles" the routes definition to a ring handler function that will route the requests to your defined handler. `router` also takes an optional 404 handler function as its 2nd argument. The 404 function must take one single argument, the http request. If no 404 handler is provided, a default 404 response will be generated.

You should use `router` in your ring app definition (with your desired middlewares).

```clojure
```



## TODO

tests

...



## License

Copyright © 2014 Tony Tam

Released under the MIT license.
