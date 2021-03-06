# Serving files
A common aspect of writing web applications is to serve files. This chapter
walks you through the way this can be done with HttpMaid.

## Serving from the filesystem
Even in times of highly dynamic websites generated by Javascript, do webservers still
 be able to serve static resources, for example static images. 
In order to serve a file with HttpMaid, call
the `setFileAsBody()` method on `HttpResponse` object.
If - for example - you want to serve an image file at `./files/image.png`, the HttpMaid
configuration could look like this:
<!---[CodeSnippet] (staticFile)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/myFile", (request, response) -> response.setFileAsBody("src/test/resources/image.jpg"))
        .build();
```


You should now see the image when browsing to http://localhost:1337/myFile.

## Serving Java resources
Sometimes, when writing web applications in Java, you would want to
serve a Java resource.
Java resources can be bundled into Java Archives (.jar-Files) and
therefore offer distinct advantages in regard to software delivery and deployment.
Resource loading can be tricky to do right,
so in order to make your life as easy as possible, HttpMaid offers
a convenient integration for handling requests with resources.
Just provide the resource path via the `setJavaResourceAsBody()` to
the `HttpResponse`, and HttpMaid will do the rest.

Assume you create a resource file called `myHtmlResource.html` in
your classpath with the following content:
<!---[CodeSnippet] (file=../examples/documentation/src/test/resources/myHtmlResource.html)-->
```
<!--
  ~ Copyright (c) 2020 Richard Hauswald - https://quantummaid.de/.
  ~
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

<html>
<h1>My Html Resource</h1>
This is some example content.
</html>
```

Now, to serve this resource file using HttpMaid, we could use this configuration:
<!---[CodeSnippet] (javaResource)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/resource", (request, response) -> response.setJavaResourceAsBody("myHtmlResource.html"))
        .build();
```

Once the HttpMaid instance is started as usual, you can access
http://localhost:1337/resource with a browser and
see the html page rendered.

## Setting the filename of downloads
Sometimes you want to provide a file explicitly as a download, i.e. you want the browser to
prompt the user to store the served file somewhere on the local file system.
In those cases, usability benefits from providing a name suggestion for the file to be stored. 
Imagine a PDF that the client browser should present to the user as a download. In this case, depending
on the context, the name `report.pdf` might be a sensible choice.
In order to achieve this in http, the intended name suggestion needs to be encoded into to value of the `Content-Disposition` header
of the response.
The `HttpResponse` object of the `HttpHandler` interface
conveniently offers a `asDownloadWithFilename()` method to perform this task.
<!---[CodeSnippet] (contentDisposition)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/myDownload", (request, response) -> {
            response.setBody("Hello World");
            response.asDownloadWithFilename("hello-world.txt");
        })
        .build();
```

When you browse to http://localhost:1337/myDownload, instead of seeing the `Hello World` message displayed, a dialog
should have popped up offering to store the file under the name `hello-world.txt`.

## Caching and advanced features
HttpMaid was intended to facilitate architecturally sane web applications, first and foremost APIs.
The implementation of static file handling has never been a priority. Therefore, it might
currently lack features like support for caching. Nonetheless, if you need this feature or other features to exist,
please fell free to open a feature request at <a href="https://github.com/quantummaid/httpmaid/issues" target="_blank">Github</a>.

