# AngularJS directives for [PatternFly](https://www.patternfly.org)

This project will provide a set of common AngularJS directives for use with the PatternFly reference implementation.

## Getting started

You have to install required software before you're able to use grunt:

* Install Node.js - Find more information on [Node.js](http://nodejs.org/)
* Install npm - If npm is not already installed with Node.js, you have to install it manually. Find more information on [NPM](https://www.npmjs.org/)
* Install Bower - Find more information on [Bower](http://bower.io/)
* Install Grunt - Find more information on [Grunt](http://gruntjs.com/)
* Install npm dependencies with:
```shell
npm install
```
* Install bower dependencies with:
```shell
bower install
```

You should have your environment ready now. To see which grunt tasks are available, type:
```shell
grunt help
```
## API documentation

The API documentation can be built with:
```shell
grunt ngdocs
```

If you're interested in reading the docs right away, you can use special target, which will start a web server:
```shell
grunt ngdocs:view
```

After executing this tasks you'll be able to access the documentation at [http://localhost:8000/](http://localhost:8000/).