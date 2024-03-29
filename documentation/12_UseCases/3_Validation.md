# Usecases III: Validation
Let's add another usecase to the multiplication example. One obvious additional feature
would be to support division.
The accompanying usecase is fairly trivial:
<!---[CodeSnippet] (divisionUseCasStep1)-->
```java
public final class DivisionUseCase {

    public CalculationResponse divide(final DivisionRequest divisionRequest) {
        final int divisor = divisionRequest.divisor;
        final int result = divisionRequest.dividend / divisor;
        return calculationResult(result);
    }
}
```

with the corresponding `DivisionRequest`:
<!---[CodeSnippet] (divisionRequestStep1)-->
```java
public final class DivisionRequest {
    public final Integer dividend;
    public final Integer divisor;

    private DivisionRequest(final Integer dividend, final Integer divisor) {
        this.dividend = dividend;
        this.divisor = divisor;
    }

    public static DivisionRequest divisionRequest(final Integer dividend,
                                                  final Integer divisor) {
        return new DivisionRequest(dividend, divisor);
    }
}
```

To add this usecase to HttpMaid, all you need to do is add one single line:
<!---[CodeSnippet] (divisionExampleStep1)-->
```java
final Gson GSON = new Gson();
final HttpMaid httpMaid = anHttpMaid()
        .post("/multiply", MultiplicationUseCase.class)
        .post("/divide", DivisionUseCase.class)
        .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
        .build();
```

Since the `DivisionRequest` class resides in the same package as `MultiplicationRequest`,
the MapMaid configuration from the previous example will auto-detect the new class and does
not need to be changed.

You can try it out with the following curl command:

<!---[CodeSnippet] (file=../../examples/documentation/src/test/resources/division1.curl)-->
```
$ curl --request POST --header 'Content-Type: application/json' --data '{"divisionRequest": {"dividend": "12", "divisor": "3"}}' http://localhost:1337/divide
```

The output will be the correct result to the division of 12 by 3:
```json
{"result":"4"}
```

## Illegal input
Now, school taught every single one of us that there is one highly illegal thing you should never ever even think of doing: dividing by zero.
Let's go:
<!---[CodeSnippet] (file=../../examples/documentation/src/test/resources/division2.curl)-->
```
$ curl --request POST --header 'Content-Type: application/json' --data '{"divisionRequest": {"dividend": "12", "divisor": "0"}}' http://localhost:1337/divide
```

Unlike us, Java actually respects the laws of math and we should see the following exception on the console:
```
ERROR: java.lang.ArithmeticException: / by zero
```
with an accompanying stacktrace.

Obviously, our calculation application should be ready to deal with illegal input like this and handle it accordingly.
As with every unhandled exception in HttpMaid, it is logged (to `STDERR` by default) and the request is answered with
an empty status code `500` (Internal Server Error) response.
This might not be particularly useful to the user, since a potential frontend needs to be able to tell
what exactly was wrong about the provided input. Otherwise, the user would not know how to correct it.

## Validating input
One way to tell the user what went wrong would be normal HttpMaid exception mapping.
It has already been explained in previous chapters how this can be achieved.
An obvious downside to this approach is that we need to execute the actual division to trigger the `ArithmeticException`
and know that the input was wrong.
By the time we execute the divsion, our input should already have been validated.
This might not be obvious for the divison, but think of a more mature example, where the usecase
would not consist of a simple math operation, but of database queries with potentially corrupt data.
To mitigate this problem, we can validate the divisor in the `DivisionRequest`, so the exception would
be thrown before the usecase and calculation could be called:
<!---[CodeSnippet] (divisionRequestStep2)-->
```java
public final class DivisionRequest {
    public final Integer dividend;
    public final Integer divisor;

    public DivisionRequest(final Integer dividend, final Integer divisor) {
        this.dividend = dividend;
        this.divisor = divisor;
    }

    public static DivisionRequest divisionRequest(final Integer dividend,
                                                  final Integer divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("the divisor must not be 0");
        }
        return new DivisionRequest(dividend, divisor);
    }
}
```


Let's devide through zero again:

<!---[CodeSnippet] (file=../../examples/documentation/src/test/resources/division2.curl)-->
```
$ curl --request POST --header 'Content-Type: application/json' --data '{"divisionRequest": {"dividend": "12", "divisor": "0"}}' http://localhost:1337/divide
```

Now, we see an `UnrecognizedExceptionOccurredException` on the console. This is actually an exception of the MapMaid project,
with our `IllegalArgumentException` down below the stacktrace as the causing exception:
```
Caused by: java.lang.IllegalArgumentException: the divisor must not be 0
```
This means that MapMaid is not prepared to see this particular exception i.e. it does not recognise it. In this case, MapMaid will abort and
re-throw the exception wrapped in the observed `UnrecognizedExceptionOccurredException` since that is the only safe thing to do.
However, if we tell MapMaid that an `IllegalArgumentException` is to be expected and that it indicates a failed
validation, MapMaid will behave differently. Let's tell MapMaid about the exception:

<!---[CodeSnippet] (divisionExampleStep2)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .post("/multiply", MultiplicationUseCase.class)
        .post("/divide", DivisionUseCase.class)
        .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
        .configured(withMapperConfiguration(mapMaidBuilder -> {
            mapMaidBuilder.withExceptionIndicatingValidationError(IllegalArgumentException.class);
        }))
        .build();
```

Now, when we devide by zero (again):

<!---[CodeSnippet] (file=../../examples/documentation/src/test/resources/division2.curl)-->
```
$ curl --request POST --header 'Content-Type: application/json' --data '{"divisionRequest": {"dividend": "12", "divisor": "0"}}' http://localhost:1337/divide
```

we actually get something meaningful out of it:
```json
{"errors":[{"path":"","message":"the divisor must not be 0"}]}
```
MapMaid will gather all recognised validation exceptions and present all of them to us in a format that
is easy to process.


## Custom primitives
Let's take a deeper look at MapMaid's validation output:
```json
{
   "errors":[
      {
         "path":"",
         "message":"the divisor must not be 0"
      }
   ]
}
```
Under the key `error`, we find a list of all occurred exceptions. Since only
the `DivisionRequest` object has thrown one, the list has only one single entry.
The entry has two fields. Under the key `message`, the message of the caught exception
is stored.
The key `path` is of more interest. Under it, MapMaid stores the logical location where
the exception occured.
This can be used to easily highlight the input fields in a user form that have been filled in incorrectly.
Since the validation exception is thrown in the top level class `DivisionRequest`, the path is empty.
Ideally, we want it to contain the value `divisor`, since this is the field that is validated.
In order to achieve this, the `IllegalArgumentException` needs to be thrown in this exact field.
Currently, it is thrown in the `DivisionRequest` class.
In order for it to be located in the `divisor` field, we need to throw the exception in the class of this field.
Unfortunately, the divisor's data type is `Integer` and we cannot change the implementation of this class.
To solve the problem, we need to write our own `Divisor` data type that contains the validation:
<!---[CodeSnippet] (divisor)-->
```java
public final class Divisor {
    private final int value;

    private Divisor(final int value) {
        this.value = value;
    }

    public static Divisor parseDivisor(final String divisorAsString) {
        final int value = parseInt(divisorAsString);
        if (value == 0) {
            throw new IllegalArgumentException("the divisor must not be 0");
        }
        return new Divisor(value);
    }

    public int value() {
        return value;
    }

    public String stringValue() {
        return String.valueOf(value);
    }
}
```


We will call these kinds of classes *custom primitives* throughout this guide since
they act pretty much the same as primitive data types like `int`, `double`, or even `String`
(which is technically not a primitive data type but it is used like one).
In the world of Domain-Driven Design (DDD) they are also called *value objects*,
but it does not really matter how you call them. 
They encapsulate all aspects of a specific type of data and make sure that its
value is valid. We can now change the `DivisionRequest` accordingly:


<!---[CodeSnippet] (divisionRequestStep3)-->
```java
public final class DivisionRequest {
    public final Integer dividend;
    public final Divisor divisor;

    private DivisionRequest(final Integer dividend, final Divisor divisor) {
        this.dividend = dividend;
        this.divisor = divisor;
    }

    public static DivisionRequest divisionRequest(final Integer dividend,
                                                  final Divisor divisor) {
        return new DivisionRequest(dividend, divisor);
    }
}
```

Also the `DivisionUseCase` needs a small change:
<!---[CodeSnippet] (divisionUsecaseStep3)-->
```java
public final class DivisionUseCase {

    public CalculationResponse divide(final DivisionRequest divisionRequest) {
        final int divisor = divisionRequest.divisor.value();
        final int result = divisionRequest.dividend / divisor;
        return calculationResult(result);
    }
}
```


When you once again request the division by zero like this:

<!---[CodeSnippet] (file=../../examples/documentation/src/test/resources/division2.curl)-->
```
$ curl --request POST --header 'Content-Type: application/json' --data '{"divisionRequest": {"dividend": "12", "divisor": "0"}}' http://localhost:1337/divide
```

you will receive the following validation output:
```json
{"errors":[{"path":"divisor","message":"the divisor must not be 0"}]}
```
This time, the path correctly points to the affected field: `divisor`.
It can easily be used in a frontend to present the according form validation to
the user.

## Advanced settings
When HttpMaid handles a validation error as seen above, it will set the status code to `400` (Client Error). You can tell
HttpMaid to use another status code like this:

<!---[CodeSnippet] (setValidationErrorStatusCode)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
        .post("/", SomeUseCase.class)
        .configured(withMapperConfiguration(mapMaidBuilder ->
                mapMaidBuilder.withExceptionIndicatingValidationError(SomeValidationException.class)))
        .configured(toSetStatusCodeOnMapMaidValidationErrorsTo(401))
        .build();
```

If you do not want HttpMaid to create detailed responses for validation errors, you can disable that
feature. HttpMaid will instead throw an exception that can be handled accordingly.
Example:

<!---[CodeSnippet] (disableAggregation)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .post("/", SomeUseCase.class)
        .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
        .configured(withMapperConfiguration(mapMaidBuilder ->
                mapMaidBuilder.withExceptionIndicatingValidationError(SomeValidationException.class)))
        .configured(toNotCreateAnAutomaticResponseForMapMaidValidationErrors())
        .configured(ExceptionConfigurators.toMapExceptionsOfType(AggregatedValidationException.class, (exception, request, response) -> {
            // handle validation errors here
        }))
        .build();
```
