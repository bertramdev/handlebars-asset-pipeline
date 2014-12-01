Handlebars Asset-Pipeline
=========================
[![Build Status](https://travis-ci.org/bertramdev/handlebars-asset-pipeline.svg?branch=master)](https://travis-ci.org/bertramdev/handlebars-asset-pipeline)

Overview
--------
The JVM `handlebars-asset-pipeline` is a plugin that provides handlebars template precompiler support to asset-pipeline.

For more information on how to use asset-pipeline, visit [here](http://www.github.com/bertramdev/asset-pipeline).

Installation
------------

Simply add this plugin to your classpath in gradle or dependencies list depending on how you are using it

```gradle
//Example build.gradle file
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.bertramlabs.plugins.asset-pipeline-gradle:2.0.6'
    classpath 'com.bertramlabs.plugins.less-asset-pipeline:2.0.4'
  }
}
```

Usage
-----

Simply create files in your standard `assets/javascripts` folder with extension `.handlebars` or `.hbs`.
By default the templateRoot for your template names is specified as 'templates'. This means that any handlebars file within the root assets/javascripts folder will utilize its file name (without the extension) as its template name. Or a file in `templates/show.handlebars` would be named `templates/show`. If templates is set as the templateRoot than it would be named `show`

It is also possible to change the template path seperator for templatenames to be used by handlebars:


Gradle Example:

```groovy
assets {
	config = [
		handlebars: [
			templateRoot: 'templates',
			templatePathSeperator: '/'
		]
	]
}
```

Grails Example:
```groovy
grails {
	assets {
		handlebars {
			templateRoot = 'templates'
			templatePathSeperator = "/"
		}
	}
}
```

To use the handlebars runtime simply add handlebars js to your application.js or your gsp file

```javascript
//=require handlebars
```


Using in the Browser
--------------------

Template functions are stored in the `Handlebars.templates` object using the template name. If the template name is
`person/show`, then the template function can be accessed from `Handlebars.templates['person/show']`. See the Template Names section for how template names are calculated.

See the [Handlebars.js website](http://handlebarsjs.com/) for more information on using Handlebars template functions.
