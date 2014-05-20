# paths

Enhanced routing library for Clojure/Ring.

## Example



## Installation



## Usage



## Re: Implementation



## Notes

Thought about two ways to define routes.

\#1: A less verbose, data structure based route definition. Less typing for subpaths but also a bit more complicated to read

```clojure
(def routes1
  [
    "/" {:get 0}
    "/test/route" {:get 10}
    "/sign" {:get 20} [
        "/in" {:get 21} "/out" {:get 22} "/up" {:get 23}
    ]
    "/user" {:get 30} [
        "/:id" {:get 31} [
            "/account" {:get 311} [
                "/:id" {:get 3111}
            ]
            "/tag" {:get 312} [
                "/:id" {:get 3121}
            ]
        ]
    ]
    "/about" {:get 40}
  ])
```

\#2: Easier to read and define. But more typing.

```clojure
(def routes2
  [
    "/" {:get 0}
    "/test/route" {:get 10}
    "/sign" {:get 20}
    "/sign/in" {:get 21}
    "/sign/out" {:get 22}
    "/sign/up" {:get 23}
    "/user" {:get 30}
    "/user/:id" {:get 31}
    "/user/:id/account" {:get 311}
    "/user/:id/account/:id" {:get 3111}
    "/user/:id/tag" {:get 312}
    "/user/:id/tag/:id" {:get 3121}
    "/about" {:get 40}
  ])
```

## License

Copyright © 2014 Tony Tam

Released under the MIT license.
