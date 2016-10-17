vert.x-utils
============

<a href="https://raw.githubusercontent.com/groupon/vertx-utils/master/LICENSE">
    <img src="https://img.shields.io/hexpm/l/plug.svg"
         alt="License: Apache 2">
</a>
<a href="https://travis-ci.org/groupon/vertx-utils/">
    <img src="https://travis-ci.org/groupon/vertx-utils.png"
         alt="Travis Build">
</a>
<a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.groupon.vertx%22%20a%3A%22vertx-utils%22">
    <img src="https://img.shields.io/maven-central/v/com.groupon.vertx/vertx-utils.svg"
         alt="Maven Artifact">
</a>
<a href="http://javadoc.io/doc/com.groupon.vertx/vertx-utils">
    <img src="http://javadoc.io/badge/com.groupon.vertx/vertx-utils.svg" 
         alt="Javadocs">
</a>

A collection of utilities for Vert.x which allow for simplified deployment, standardized logging, rescheduling of handlers, and a file based health check handler.

Usage
-----

## RescheduleHandler

Provides the ability to execute a handler on a timer and reschedule on completion.

```java
RescheduleHandler rescheduleHandler = new RescheduleHandler(vertx, event -> { }, 1000);
vertx.setTimer(1000, rescheduleHandler);
```

## MainVerticle

Used to deploy a configurable number of instances of different verticles and enforce dependencies between the verticles.

Example mainConf.json:

```json
{
  "verticles": {
    "MetricsVerticle": {
      "class": "com.groupon.vertx.utils.MetricsVerticle",
      "instances": 1,
      "worker": true,
      "config": { }
    },
    "ExampleVerticle": {
      "class": "com.groupon.example.verticle.ExampleVerticle",
      "instances": 1,
      "worker": true,
      "config": { },
      "dependencies": [ "MetricsVerticle" ]
    }
  }
}
```

Example mod.json:

```json
{
  "main": "com.groupon.vertx.utils.MainVerticle",
  "preserve-cwd": true,
  "worker": false
}
```

Starting Vert.x with MainVerticle

```text
java -cp conf:lib/* org.vertx.java.platform.impl.cli.Starter runmod com.groupon.example~release -instances 1 -conf conf/mainConf.json
```

## Logger

A wrapper to provide standardized logging calls using slf4j and the com.arpnetworking.logback.StenoMarker

```java
Logger log = Logger.getLogger(Example.class);
log.info("sampleMethod", "Hello logging world!");
```

## Configuration

In the MainVerticle's configuration each child verticle's _config_ key is either an inline JSON object or a 
String representing a file path. If you specify a file path you may also specify a custom parser with the 
system property vertx-utils.config-parser-class-name (e.g. _-Dvertx-utils.config-parser-class-name=com.example.MyParser_).
This parser receives the file contents and is required to return a Vert.x JsonObject. Use it to translate
your preferred configuration (e.g. hocon, properties, yaml, xml, etc.) into JSON.

Building
--------

Prerequisites:
* [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven 3.3.3+](http://maven.apache.org/download.cgi)

Building:

    vertx-utils> mvn verify

To use the local version you must first install it locally:

    vertx-utils> mvn install

You can determine the version of the local build from the pom file.  Using the local version is intended only for testing or development.


License
-------

Published under Apache Software License 2.0, see LICENSE

&copy; Groupon Inc., 2015
