# Marshalling I: Forms
You should by now have a basic understanding of setting up a simple
HttpMaid instance with request handlers and know how to connect
it to an endpoint. This chapter will show you how HttpMaid
can help you with handling forms.

## Forms
One recurring challenge in web applications is the processing of forms.
To start, let's create a form as a Java resource named `form.html` somewhere
in the classpath:
<!---[CodeSnippet] (file=../../examples/documentation/src/main/java/de/quantummaid/httpmaid/documentation/xx_marshalling/form.html)-->
```
<html>
<body>
<form action="/submit" method="post">
    Name:<input type="text" name="name"><br>
    Profession:<input type="text" name="profession">
    <input type="submit" value="Send">
</form>
</body>
</html>
```

The form will consist of two textfields (name and profession)
and a button to submit the form.
The following code creates a HttpMaid instance to serve the form:
<!---[CodeSnippet] (formMarshallingStep1)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
        .post("/submit", (request, response) -> response.setBody(request.bodyString()))
        .build();
```

We configured two routes in the endpoint. The first one serves every `GET` request
to the `/form` route with our html resource.
The second one accepts `POST` requests to the `/submit` route, since the
`<form action="/submit" method="post">` line in the html suggests that
submitted forms will be sent there. The handler of this route only returns
the body of the request, so we can inspect it.

Once started, the form should be available at http://localhost:1337/form.
When you now submit the form with `Bob` as name and `Developer` as profession,
you should see the following message sent back:
```
name=Bob&profession=Developer
```
This type of key-value encoding is called *form encoded* (or `application/x-www-form-urlencoded`,
to be specific). 

Now, just returning the form encoded form submission to the sender is not
a very reasonable way to handle forms. In order to handle it productively,
we need a way to decode the key-value pairs that we received.
Luckily, HttpMaid is able to this for us with an operation
called *unmarshalling*. 

## What is unmarshalling?
The concept of *unmarshalling* in the context of HttpMaid is the process of
translating a `String` of a given format (in our case: *form encoded*) into a `Map<String, Object>`
that contains all the values encoded in the `String`.
For us and our form example, this means we will receive a `Map` with two
entries:
```
"name" = "Bob"
```
and
```
"profession" = "Developer"
```
which is exactly what we need.

## Configuring unmarshalling
Now we need to tell HttpMaid to actually unmarshall requests from the *form encoded* format.
We can easily do that using the `MarshallingConfigurators.toUnmarshallFormUrlEncodedRequests()` configurator
method:
<!---[CodeSnippet] (formMarshallingStep2)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
        .post("/submit", (request, response) -> response.setBody(request.bodyString()))
        .configured(toUnmarshallFormUrlEncodedRequests())
        .build();
```

This way, HttpMaid will automatically detect any form encoded bodies
and unmarshall them.

## The body map
In order to access the unmarshalled form contents, the `HttpRequest` object
offers a method `bodyMap()`. Let's use this to change our handler to something
slightly more productive:
<!---[CodeSnippet] (formMarshallingStep3)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
        .post("/submit", (request, response) -> {
            final Map<String, Object> bodyMap = request.bodyMap();
            final String name = (String) bodyMap.get("name");
            final String profession = (String) bodyMap.get("profession");
            response.setBody("Hello " + name + " and good luck as a " + profession + "!");
        })
        .configured(toUnmarshallFormUrlEncodedRequests())
        .build();
```

When you now submit the form again with the values `Bob` and `Developer`,
the resulting web page should look like this:
```
Hello Bob and good luck as a Developer!
```

<!---[Nav]-->
[&larr;](../10_MultipartUploads.md)&nbsp;&nbsp;&nbsp;[Overview](../../README.md)&nbsp;&nbsp;&nbsp;[&rarr;](2_MarshallingAPIs.md)
