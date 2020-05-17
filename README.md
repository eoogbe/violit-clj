# Violit - Clojure

> An internet forum for discussion

This is a hobby project created purely for my own entertainment.

## Development

### Getting started

Copy the app into a directory.

To pull the dependencies:

```
yarn
```

### `yarn start`

Runs the app in the development mode.  
Navigate to http://localhost:9630 and turn on the main build. Open
http://localhost:8000 to view the app in the browser.

The page will reload if you make edits.

### REPL
The REPL is hosted at localhost with port 9000.

If you’re using IntelliJ, edit your run configurations and add a
"Clojure → Remote REPL". Enter the host and port.

Once you connect to the network REPL, select the build you want to work
against. (You must already have that application running in a browser.) Run:

```
user=> (shadow/repl :main)
```

This will change to a CLJS REPL connected to your running application.

## License

Copyright 2022 Google LLC

[Licensed under the Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

This is not an official Google product.
