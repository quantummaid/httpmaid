# QuickStart

## The simplest sample
The most basic usage of HttpMaid is to start a server with a single route `/hello` 
and let it answer with a formal greeting `hi.`
The following code shows the complete Java code:
<!---[CodeSnippet] (quickstart)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/hello", (request, response) -> response.setBody("Hi."))
        .build();
final PureJavaEndpoint endpoint = pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
```
You can navigate your browser to `http://localhost:1337/hello` 
or you can execute the following curl command `curl http://localhost:1337/hello`.
In both cases you will be greeted with a pleasant message: `Hi.`

## How to run the simplest sample
The easiest way is to use Maven's 
[Archetypes](https://maven.apache.org/guides/introduction/introduction-to-archetypes.html).
Maven archetypes allows to share project structures over a central repository, Maven Central
in this case. Just run the following code to install the quickstart archetype. This will
create a directory with a complete Maven structure, all required dependencies and 
a `Quickstart.java` ready to be executed.

```
mvn archetype:generate -DarchetypeGroupId=de.quantummaid.httpmaid.examples  \ 
                       -DarchetypeArtifactId=archetype-quickstart           \
                       -DarchetypeVersion=1.0.28                  \
                       -DgroupId=de.quantummaid.examples                    \
                       -DartifactId=httpmaid-quickstart                     \
                       -Dversion=1.0.0                                      \
                       -Dpackaging=java                                     \
 ```

You will see the following output with a prompt at the end:

```
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------< org.apache.maven:standalone-pom >-------------------
[INFO] Building Maven Stub Project (No POM) 1
[INFO] --------------------------------[ pom ]---------------------------------
[INFO] 
[INFO] >>> maven-archetype-plugin:3.1.2:generate (default-cli) > generate-sources @ standalone-pom >>>
[INFO] 
[INFO] <<< maven-archetype-plugin:3.1.2:generate (default-cli) < generate-sources @ standalone-pom <<<
[INFO] 
[INFO] 
[INFO] --- maven-archetype-plugin:3.1.2:generate (default-cli) @ standalone-pom ---
[INFO] Generating project in Interactive mode
[INFO] Archetype repository not defined. Using the one from [de.quantummaid.httpmaid.examples:archetype-quickstart:1.0.28] found in catalog local
[INFO] Using property: groupId = de.quantummaid.examples
[INFO] Using property: artifactId = httpmaid-quickstart
[INFO] Using property: version = 1.0.0
[INFO] Using property: package = de.quantummaid.examples
Confirm properties configuration:
groupId: de.quantummaid.examples
artifactId: httpmaid-quickstart
version: 1.0.0
package: de.quantummaid.examples
 Y: : 
```
By pressing `Y` and Enter, the quickstart project will be created.
Executing the `main` function of the `Quickstart.java` will start the example server.

Since the sample is so simple, it can be run from the commandline.

First change into the project root directory and let Maven build the project:

```
mvn clean verify
```

Then you can call java with the correct class and HttpMaid as dependency 
(assuming you a Linux system)
```
java -classpath target/classes:$HOME/.m2/repository/de/quantummaid/httpmaid/core/1.0.28/core-1.0.28.jar de.quantummaid.examples.QuickStart
```
In case you are on a Windows replace `$HOME/.m2` in the command above with the absolute path to your Maven
home directory. 


## Adding HttpMaid the old school way
To use HttpMaid you need to declare it as a dependency.
This guide assumes you are using Maven, but it will work with Gradle etc. accordingly.
To use HttpMaid add its dependency to your pom.xml:
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid</groupId>
    <artifactId>core</artifactId>
    <version>1.0.28</version>
</dependency>
```

## Explanation of the simplest sample
The sample configured a single route:
<!---[CodeSnippet] (quickstartPart1)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/hello", (request, response) -> response.setBody("Hi."))
        .build();
```

This will respond to all GET requests to the `/hello` route with the string `"hi!"`


HttpMaid by itself is infrastructure agnostic. When you want to actually serve
requests, you have to combine it with an endpoint. There are endpoints for
different ways to serve http requests, like using a standalone Jetty instance
or integrating into a servlet engine. In our example, we will facilitate the
so-called `PureJavaEndpoint`, which is implemented on top of the native Java http
server and has therefore a minimal dependency footprint:
<!---[CodeSnippet] (quickstartPart2)-->
```java
final PureJavaEndpoint endpoint = pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
```

You can now point a web browser of your choice to http://localhost:1337/hello and
see the expected `"hi!"` message.

When you are serving http requests, you can easily clean up all resources like so:
<!---[CodeSnippet] (quickstartPart3)-->
```java
httpMaid.close();
```

This will close all resources, including the endpoint.