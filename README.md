# chat-client

My test project for [Igeolise job application](http://functionaljobs.com/jobs/8769-web-frontend-developer-at-igeolise) i found on the FunctionalJob site. 

#### Task definition

Write a web client application for given chat server. It uses web sockets and JSON to communicate.

#### Requirements

* Responsive and user friendly design. It should work equally well on desktop, tablet or mobile phone. It should also be nice to use and look at.
* Clean, testable code. You don't have to provide full test cases, but you have to prove it is testable.

#### Solution

I used [Clojurescript](https://github.com/clojure/clojurescript) as my choice of programming language,  ["Reagent"](http://reagent-project.github.io/) for the functionality and [Bootstrap/Material](https://fezvrasta.github.io/bootstrap-material-design/) for the out-of-box responsive design.

Code implements all functionalities described in [README.txt](http://www.igeolise.com/jobs/webdev/server-README) file and includes test example with a very simple showcase of testing a Reagent component.

As it is just a demo project, it handles no failure or any edge case that may occur.

## Usage

If [Igeolise chatserver](http://www.igeolise.com/jobs/webdev/play-scala-1.0-SNAPSHOT.zip) is already running, then just open `resources/index.html` and you can start using this simplistic client without any hustle.

## Development

You must have [Leiningen](http://leiningen.org/#install) pre-installed to make commands below work.

1. Make it better.
2. Compile development version. ( *it's big ~2MB, not for production* )

```
> lein cljsbuild once dev
```

Check changes on your browser.
(ps: This project is very plain, for better development experience please add [Weasel](https://github.com/tomjakubowski/weasel) & [Figwheel](https://github.com/bhauman/lein-figwheel) )


## Tests
 [![Build Status](https://travis-ci.org/timgluz/chat-client.svg?branch=master)](https://travis-ci.org/timgluz/chat-client)

For running tests locally, use this command

```
> lein cljsbuild test unit
```


## License
L&H - learn & hack as much you want;
 

