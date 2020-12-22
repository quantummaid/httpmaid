# Usecases II: Object Mapping

In the first part of the usecases chapter, we looked at the ping usecase which
is actually not very useful.
It does not receive any parameters and does not return anything.
In this chapter, we will create a more realistic scenario in a webservice
that offers the multiplication of two integers.

## Parameters and Return Values
Let's start with defining the use case:
<!---[CodeSnippet] (multiplicationUseCase)-->
```java
public final class MultiplicationUseCase {

    public CalculationResponse multiply(final MultiplicationRequest multiplicationRequest) {
        final int result = multiplicationRequest.factor1 * multiplicationRequest.factor2;
        return calculationResult(result);
    }
}
```

This usecase takes an object of type `MultiplicationRequest` as parameter:
<!---[CodeSnippet] (multiplicationRequest)-->
```java
public final class MultiplicationRequest {
    public final Integer factor1;
    public final Integer factor2;

    private MultiplicationRequest(final Integer factor1, final Integer factor2) {
        this.factor1 = factor1;
        this.factor2 = factor2;
    }

    public static MultiplicationRequest multiplicationRequest(final Integer factor1,
                                                              final Integer factor2) {
        return new MultiplicationRequest(factor1, factor2);
    }
}
```

As you can see, a `MultiplicationRequest` simply encapsulates two factors, each of with
having the datatype `Integer`.
The `MultiplicationUseCase` will then take both factors, multiply them, and return the result
encapsulated in an object of type `CalculationResponse`:
<!---[CodeSnippet] (calculationResponse)-->
```java
public final class CalculationResponse {
    public final Integer result;

    private CalculationResponse(final Integer result) {
        this.result = result;
    }

    public static CalculationResponse calculationResult(final Integer result) {
        return new CalculationResponse(result);
    }
}
```

The `MultiplicationUseCase` is structured in the same way as the `PingUseCase` (a public constructor and one public method)
and again does not contain a single dependency on any infrastructure code.

We can now add the usecase to our configuration:
<!---[CodeSnippet] (multiplicationUseCaseWithoutMappingExample)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .post("/multiply", MultiplicationUseCase.class)
        .build();
```

If we would start the application now, a `POST` request to `/multiply` would fail, because HttpMaid
does not yet know how to create the `MultiplicationRequest` parameter and what to do with the `CalculationResponse`
return value.

Sending a `POST` request using curl
<!---[CodeSnippet] (file=../../examples/documentation/src/test/resources/multiply1.curl)-->
```
$ curl --request POST http://localhost:1337/multiply
```

would result in a `java.lang.NullPointerException`.

## Object mapping
Until this point, it is unclear how HttpMaid could get the `MultiplicationRequest` parameter needed to call
`multiply()`.
One possible and common way to achieve this is to put the input data into the request body
and expect the result to be sent back in the response body.
If we encode the data using Json, an example request body could probably look like this:
```json
{
  "factor1": "4",
  "factor2": "5"
}
```
and the corresponding response body would look like this:
```json
{
  "result": "20"
}
```

Now we need a way to reflect this in HttpMaid. As we know from the marshalling chapters, HttpMaid integrates well with its
sister project MapMaid.
We also know that MapMaid is able to (un-)marshall requests and responses i.e. can convert a `String` to a `Map<String, Object>`
and vice versa.
What we didn't tell you until now is that
it can go even one step further and do conversions from a `Map<String, Object>` to domain objects and vice versa.
This additional step is called deserialization and serialization. 
Together, MapMaid forms this workflow for unmarshalling (a) and deserialization (b):
```
Request Body    -(a)->    Map<String, Object>    -(b)->    Domain Object
```
and this workflow for serialization (c) and marshalling (d):
```
Domain Object    -(c)->    Map<String, Object>    -(d)->    Response Body
```

In fact, MapMaid is so powerful at doing (de-)serialization that we just need to add
the following dependency and it will automatically and intelligently determine how
to (de-)serialize our domain objects (`MultiplicationRequest`, `CalculationResponse` and `Number`):
<!---[Dependency](groupId=de.quantummaid.httpmaid.integrations artifactId=httpmaid-mapmaid version)-->
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid.integrations</groupId>
    <artifactId>httpmaid-mapmaid</artifactId>
    <version>0.9.111</version>
</dependency>
```
Please refer to MapMaid's documentation if you want to learn more about this feature.
Using Gson for marshalling, we end up with a very lean and readable configuration:
<!---[CodeSnippet] (multiplicationUseCaseWithMappingExample)-->
```java
final Gson GSON = new Gson();
final HttpMaid httpMaid = anHttpMaid()
        .post("/multiply", MultiplicationUseCase.class)
        .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
        .build();
```

You can try the configuration with the following curl command:

<!---[CodeSnippet] (file=../../examples/documentation/src/test/resources/multiply2.curl)-->
```
$ curl --request POST --header 'Content-Type: application/json' --data '{"factor1": "3", "factor2": "4"}' http://localhost:1337/multiply
```

And see the correct result for the multiplication of 3 and 4:
```json
{"result":"12"}
```
