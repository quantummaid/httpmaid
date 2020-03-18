# Usecases IV: Advanced topics

You now know how to configure HttpMaid to directly serve usecases and
how to facilitate the MapMaid integration to map request and responses
to usecase parameters and return values. This chapter will talk about
some aspects that arise in most projects and how to configure aspects that
are more advanced. 

## Mapping headers/query parameters/etc.
In the second usecases chapter we showed you how to map
a request body to usecase parameters and the usecase's return value
to the response body.
This is not the only way http allows data to be communicated between the caller on
the client side and the application on server side.
Given the multiplication example from the previous chapter,
crafting a `POST` request to access the webservice might not be the optimal choice.
Another option would be encoding
the `factor1` and `factor2` fields into the url itself as query parameters.
Depending on the circumstances, this could be a more usable approach
since it's accessible from a normal web browser.
Either way, HttpMaid has you covered.

### Enriching requests with other data
Let's first look at request processing i.e. parsing the usecase parameters.
The established workflow looks like this:
```
Request Body    --->    Map<String, Object>    --->    Domain Object
```
If we now want to include other aspect of the request (headers, query parameters,
path parameters, authentication data, etc.), we can enrich the request
map with exactly this data. The resulting workflow would look like
this:
```
Query Parameters, etc.  -----
                            |
                            V
Request Body    --->    Map<String, Object>    --->    Domain Object
```

To configure this enrichment in HttpMaid, the class `EventConfigurators` offers
a ton of convenient configurator methods to choose from.
You can even provide more than one of them and they are cascaded in the order they were configured.

#### mappingQueryParameter()
Enriches the request map with a query parameter.
Requires the request to contain the configured query parameter and will abort the request otherwise.

#### mappingPathParameter()
Enriches the request map with a path parameter.
Requires the request to contain the configured path parameter and will abort the request otherwise.

#### mappingHeader()
Enriches the request map with a request header.
Requires the request to contain the configured header and will abort the request otherwise.

#### mappingAuthenticationInformation()
Enriches the request map with the authentication information.
Takes as parameter a `String` that will be used as key.
Requires the request to be authenticated and will abort the request otherwise.


### Extracting response data
Now let's consider the opposite direction, where the returned domain object
gets mapped to the response body with the intermediate step of
creating a map:
```
Domain Object    --->    Map<String, Object>    --->    Response Body
```

Now, if we want some of the map to be e.g. set as a response header - instead of ending
up in the response body - we need to extract the respective data from the response
map and set the headers accordingly. This procedure is visualized in this extended workflow: 
```
Domain Object    --->    Map<String, Object>    --->    Response Body
                                    |
                                    --------------->    Headers, etc.
```

To configure the extraction, you can again choose from a variety of
configurator methods in `ÈventConfigurators`:

#### toExtractFromTheResponseMapUsing()
Takes as parameter an implementation of `ResponseMapExtractor` which consumes
the current response map and the `HttpResponse` that is associated with the current request.
You can query and/or remove arbitrary values from the map and set them as headers, etc.

#### toExtractFromTheResponseMapTheHeader()
Takes a header key and a map key as parameters - when the map key is omitted, it will be
the same as the header key. If the response map contains a value under the provided map key,
the value will be removed from the map and added as a response header value with the
provided header key.

### Example
As stated above, we can now change the multiplication example to use take its
input from the query parameters instead of the request body:
<!---[CodeSnippet] (calculationWithQueryParametersExample)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/multiply", MultiplicationUseCase.class, mappingQueryParameter("factor1"), mappingQueryParameter("factor2"))
        .get("/divide", DivisionUseCase.class, mappingQueryParameter("dividend"), mappingQueryParameter("divisor"))
        .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
        .configured(toConfigureMapMaidUsingRecipe(mapMaidBuilder -> {
            mapMaidBuilder.withExceptionIndicatingValidationError(IllegalArgumentException.class);
        }))
        .build();
```

You can point a browser to http://localhost:1337/multiply?factor1=3&factor2=4 and verify that the response
is indeed the multiplication of 3 and 4:
```json
{"result":"12"}
```

Also the division of 12 by 4 should work via the url http://localhost:1337/divide?dividend=12&divisor=4:
```json
{"result":"3"}
```

## Dependency Injection
Until now, we assumed that all usecase classes have a public constructor with
zero arguments and HttpMaid would call this constructor when instantiating the 
usecases. Of course, this assumption is often not feasible. Serious projects
oftentimes facilitate dependency injection frameworks and/or the usecase classes
have dependencies like database objects that need to be provided in the constructor.
It is very easy to reflect these requirements in the HttpMaid configuration.
In order to configure usecase instantiation to your needs and e.g. register
the injector of your choice, the `UseCaseConfigurators.toCreateUseCaseInstancesUsing()`
configurator method exists. If for example you would like
to register a Guice injector, the configuration would look like this:
<!---[CodeSnippet] (dependencyInjectionSample)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        /*...*/
        .configured(toCreateUseCaseInstancesUsing(injector::instantiate))
        .build();
```


<!---[Nav]-->
[&larr;](3_Validation.md)&nbsp;&nbsp;&nbsp;[Overview](../../README.md)&nbsp;&nbsp;&nbsp;[&rarr;](../13_CORS.md)


