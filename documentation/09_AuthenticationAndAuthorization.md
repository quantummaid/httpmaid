# Authentication and Authorization
A recurring aspect of web applications is the concept of users that have certain
permissions.
There are two requirements that arise from this goal:
authentication and authorization. Authentication makes sure
that users are who they claim they are, while authorization is the process that determines
whether the users actually have the permission to do what they intend to do.
This chapter will explain how to do both using an extended example.

## The application
This example will create a simplistic web application with two sections - a *normal* section that every user will get access to and
an *admin* section that will be reserved to select users.
<!---[CodeSnippet] (basicAuthStep1)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .get("/normal", (request, response) -> response.setBody("The normal section"))
        .get("/admin", (request, response) -> response.setBody("The admin section"))
        .build();
```

Right now, both sections can be accessed by anyone. We will change that soon.

## Creating a user database
Before we can go into HttpMaid details, we need a way to authenticate users
and query their access rights.
To keep things as abstract as possible, we will do this by interacting with the
following interface:  
<!---[CodeSnippet] (aaaUserDataBase)-->
```java
public interface UserDatabase {
    boolean authenticate(String username, String password);

    boolean hasAdminRights(String username);
}
```

This interface is just an example and the exact layout does not matter.
 You can find an in-memory implementation `InMemoryUserDatabase.java` 
 of this interface in the `examples/documentation`
submodule that provides two normal users:
- `joe` (password: `qrpk4L?>L(DBa[mN`)
- `jim` (password: `:Ce<9q=8KKjbtgfK`)

and one admin user:
- `jack` (password: `*eG)r@;{'4g'cM?3`)

The following examples will assume this implementation.
In a production scenario, the implementation will probably interact with a database
or an external service (LDAP, ActiveDirectory, etc.).

## Basic Auth
The http protocol supports a rudimentary authentication method commonly refered to as *Basic Auth*.
Basic Auth has several shortcomings and should - if at all - only be used in combination with
https. Since it does not need a custom login mechanism, we will nonetheless use it to start our discussion
of HttpMaid authentication and authorization features.
To activate it in our sample application, we will facilitate the `toDoBasicAuthWith()` configurator method
in the `SecurityConfigurators` class. It can optionally be appended with a message that is shown when the user
is asked to log in.
<!---[CodeSnippet] (basicAuthStep2)-->
```java
final UserDatabase userDatabase = new InMemoryUserDatabase();
final HttpMaid httpMaid = anHttpMaid()
        .get("/normal", (request, response) -> response.setBody("The normal section"))
        .get("/admin", (request, response) -> response.setBody("The admin section"))
        .configured(toDoBasicAuthWith(userDatabase::authenticate).withMessage("Hello, please authenticate!"))
        .build();
```

When starting the example and navigating to http://localhost:1337/normal,
the browser will open a pop-up window that will ask you for username and
password credentials with the message `Hello, please authenticate!`.
After entering the valid username and password `joe` / `qrpk4L?>L(DB\[mN`,
 you should successfully see the `The normal section`message.

You should have noticed two things: first, you did not have to provide the credentials again, because the browser caches them
for a fixed period of time (15 minutes for most browsers).
Secondly, you have access to the admin section as `joe`, although Joe is not intended to have access to it.
Obviously, we still need to implement the authorization logic.
Let's add the `toAuthorizeRequestsUsing()` configurator method (also in `SecurityConfigurators`).
<!---[CodeSnippet] (basicAuthStep3)-->
```java
final UserDatabase userDatabase = new InMemoryUserDatabase();
final HttpMaid httpMaid = anHttpMaid()
        .get("/normal", (request, response) -> response.setBody("The normal section"))
        .get("/admin", (request, response) -> response.setBody("The admin section"))
        .configured(toDoBasicAuthWith(userDatabase::authenticate).withMessage("Hello, please authenticate!"))
        .configured(toAuthorizeRequestsUsing((authenticationInformation, request) ->
                authenticationInformation
                        .map(username -> userDatabase.hasAdminRights((String) username))
                        .orElse(false))
                .onlyRequestsTo("/admin"))
        .build();
```

Note that the authorizer configurator method takes a lambda with two parameters - `authenticationInformation` and `request`.
The `request` parameter is the established `HttpRequest` object. 
The `authenticationInformation` is of type `Optional<Object>` 
and is set by the authenticator.
If the request has not been authenticated, it is empty.
If the request has been successfully authenticated, it will contain a value of a type depending on the implementation of the authenticator.
In case of the Basic Auth authenticator, it will contain the username that the user has logged in with as a `String`.
We can use this information to determine whether the user is authorized to access the admin section.
If it is set, we will query the user database, ask whether the current user is allowed to access the admin section
and return the result.
If it is not set, we will return false - unauthenticated users cannot be administrators (note that due to the way Basic Auth works,
with the current configuration, unauthenticated requests would not even reach the authorizer because they will already be rejected
by the Basic Auth authenticator).
Requests will only be allowed to continue if the authorizer returns true.
The configurator is then customized with the `.onlyRequestsTo("/admin")` line - this makes sure that it will only trigger
on the requests that actually need administrator privileges.

You can now once again start the example app, browse to http://localhost:1337/admin and enter the `joe` / `qrpk4L?>L(DB\[mN` credentials,
only this time you will be rejected with an empty page and status code `401`.

If you don't like this behaviour and would like to customize how HttpMaid responds to unauthorized requests, you can change it.
This is done by customizing the authorizer configurator with the `.rejectingUnauthorizedRequestsUsing()` method like in our example:
<!---[CodeSnippet] (basicAuthFull)-->
```java
final UserDatabase userDatabase = new InMemoryUserDatabase();
final HttpMaid httpMaid = anHttpMaid()
        .get("/normal", (request, response) -> response.setBody("The normal section"))
        .get("/admin", (request, response) -> response.setBody("The admin section"))
        .configured(toDoBasicAuthWith(userDatabase::authenticate).withMessage("Hello, please authenticate!"))
        .configured(toAuthorizeRequestsUsing((authenticationInformation, request) ->
                authenticationInformation
                        .map(username -> userDatabase.hasAdminRights((String) username))
                        .orElse(false))
                .onlyRequestsTo("/admin")
                .rejectingUnauthorizedRequestsUsing((request, response) -> response.setBody("Please login as an administrator.")))
        .build();
```

You can now again try to access the admin section, but this time you will be rejected with a much more friendly `Please login as an administrator.`
message.

## Custom login page
With Basic Auth, the browser will actually send the credentials (username and password) with every request, which
will subsequently lead to our user database being queried on every request. This might be OK with our fast
in-memory implementation, but in real world scenarios with external authentication systems, this will quickly lead
to lot of requests and become unfeasible. 
Luckily, there are more advanced and modern solutions to handle these kinds of problems.
In order to use them, we must ditch the Basic Auth mechanism, which had the convenience of providing us with a
login mechanism (the browser pop-up window).
Without it, we need to implement it ourselves. Let's create a simple login form as a Java resource in the classpath
called `login.html`:
<!---[CodeSnippet] (file=../examples/documentation/src/test/java/de/quantummaid/httpmaid/documentation/authentication/login.html)-->
```
<html>
<body>
<form action="/login" method="post">
    Username:<input type="text" name="username"><br>
    Password:<input type="password" name="password">
    <input type="submit" value="Login">
</form>
</body>
</html>
```

Since the form will be posted as `form encoded`, we will need MapMaid to unmarshall the content
to a body map (see <!---[Link] ( 11_Marshalling/1_MarshallingForms.md Marshalling)-->
[Marshalling](11_Marshalling/1_MarshallingForms.md)).
This will lead to the following configuration:
<!---[CodeSnippet] (customLoginStep1)-->
```java
final UserDatabase userDatabase = new InMemoryUserDatabase();
final HttpMaid httpMaid = anHttpMaid()
        .get("/login", (request, response) -> response.setJavaResourceAsBody("login.html"))
        .post("/login", (request, response) -> {
            final Map<String, Object> loginForm = request.bodyMap();
            final String username = (String) loginForm.get("username");
            final String password = (String) loginForm.get("password");
            if (!userDatabase.authenticate(username, password)) {
                throw new RuntimeException("Login failed");
            }
            final boolean admin = userDatabase.hasAdminRights(username);
            // TODO store login in session
        })
        .get("/normal", (request, response) -> response.setBody("The normal section"))
        .get("/admin", (request, response) -> response.setBody("The admin section"))
        // TODO add authenticator
        .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> {
                    return false; // TODO authorize
                }
        )
                .onlyRequestsTo("/admin")
                .rejectingUnauthorizedRequestsUsing((request, response) -> response.setBody("Please login as an administrator.")))
        .build();
```

We now have a login form and can verify whether the provided username and password combination is actually correct.
As indicated by the first `TODO` comment, we still lack a way of storing this result somewhere in the session.
We could just set a cookie with the value `LoggedIn=true`, but this would be insecure for obvious reasons (any user could
just set the cookie and we have no way of telling whether the cookie is legit).
Luckily, so-called [Json Web Tokens (JWT)](https://jwt.io/) exist. They are short cryptographically signed pieces of information that can only be created/signed
by someone with access to the cryptographic private key i.e. the webserver.
They can store the username (refered to as `Subject`) and whether the user has administrator access (this is called a *claim*).
If we store the username and the access rights in the JWT and set it as a cookie, we can
decrypt the token in every subsequent request and be sure that its contents are untampered and trustworthy.
In order to use this mechanism with HttpMaid, we will rely on the `io.jsonwebtoken` library to do the cryptographic heavy lifting.
Just add the following dependencies to the project:

```xml
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jsonwebtoken.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jsonwebtoken.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jsonwebtoken.version}</version>
            <scope>runtime</scope>
        </dependency>
```  
We can now store the verified username in a jwt (as `Subject`) and add a claim that indicates whether the user is an administrator.
This allows us to add an authenticator configurator that will authenticate requests based on the jwt cookie and 
authorize requests based on the stored `admin` claim.
<!---[CodeSnippet] (customLoginFull)-->
```java
final Key key = secretKeyFor(SignatureAlgorithm.HS256);
final JwtParser jwtParser = parserBuilder().setSigningKey(key).build();

final UserDatabase userDatabase = new InMemoryUserDatabase();
final HttpMaid httpMaid = anHttpMaid()
        .get("/login", (request, response) -> response.setJavaResourceAsBody("login.html"))
        .post("/login", (request, response) -> {
            final Map<String, Object> loginForm = request.bodyMap();
            final String username = (String) loginForm.get("username");
            final String password = (String) loginForm.get("password");
            if (!userDatabase.authenticate(username, password)) {
                throw new RuntimeException("Login failed");
            }
            final boolean admin = userDatabase.hasAdminRights(username);
            final String jwt = builder()
                    .setSubject(username)
                    .claim("admin", admin)
                    .signWith(key).compact();
            response.setCookie("jwt", jwt);
        })
        .get("/normal", (request, response) -> response.setBody("The normal section"))
        .get("/admin", (request, response) -> response.setBody("The admin section"))
        .configured(toAuthenticateUsingCookie("jwt", jwt -> Optional.of(jwtParser.parseClaimsJws(jwt).getBody()))
                .failingOnMissingAuthenticationOnlyForRequestsTo("/login"))
        .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> authenticationInformation
                .map(object -> (Claims) object)
                .map(claims -> (Boolean) claims.get("admin"))
                .orElse(false))
                .onlyRequestsTo("/admin")
                .rejectingUnauthorizedRequestsUsing((request, response) -> response.setBody("Please login as an administrator.")))
        .configured(withMapperConfiguration(urlEncodedMarshaller()))
        .build();
```

The authenticator `toAuthenticateUsingCookie()` will find a cookie with the name `jwt` and feed its value
into the provided authenticator lambda. If no cookie with that name is found, the authenticator is not called. 
Note how this time the authenticator lambda sets the authentication information explictly by returning
an `Optional<Object>`. If the returned `Optional<Object>` is empty, the request will not be regarded as authenticated.
If the JWT has successfully been decrypted, the body (containing the `admin` claim) will be set as authentication information by
returning it wrapped as an `Optional<Object>`.

Furthermore, this authenticator will reject all unauthenticated requests. In order to not reject unauthenticated requests to `/login`,
we made authentication optional for that route with the `.failingOnMissingAuthenticationOnlyForRequestsTo("/login")` line.

You can now start this application, login via http://localhost:1337/login, navigate to http://localhost:1337/normal
or http://localhost:1337/admin and be allowed or rejected depending on the user you logged in with.
(Just in case the credentials were: `joe`/`qrpk4L?>L(DBa[mN`, `jim`/`:Ce<9q=8KKjbtgfK`,
 `jack`/`*eG)r@;{'4g'cM?3`)


## Accessing authentication information in handlers and use cases
Often times, handlers need access to the authentication information (imagine a `/myProfile` route in a social network
that displays data based on the current user).
Any time you get access to a `HttpRequest` object, you can query the authentication information with the
`authenticationInformation()` method.
Additionally, the object offers a `authenticationInformationAs()` method which allows you to receive the authentication
method in a typesafe manner so you do not have to deal with plain `Object`s.
If you are working with use cases, you can map authentication information into the request's body map that
will be used to map use case parameters (see the corresponding chapter).

## Additional configurator methods
The `SecurityConfigurators` class contains configurator methods that work with a lot of different request features.
The following list will explain them briefly.

### Authentication
#### Available configurators

- `toDoBasicAuthWith()` - authenticates requests according to the Basic Auth standard. Unauthenticated requests are rejected
with the status code `401` (Unauthenticated) and the `WWW-Authenticate` header is set accordingly. If authenticated successfully
with the supplied lambda, the authentication information is set to the authenticated username as `String`.

- `toAuthenticateRequestsUsing()` - authenticates requests with a lambda that gets the whole `HttpRequest`.
Sets the authentication information according to the returned lambda.

- `toAuthenticateUsingOAuth2BearerToken()` - authenticates requests according to the OAuth2 standard by parsing the
`Authorization` header with the type `Bearer`. Takes a lambda that processes the parsed bearer token.
Sets the authentication information according to the returned lambda.

- `toAuthenticateUsingCookie()` - authenticates requests based on the cookie with the supplied name.
Takes a lambda that processes the cookie (if present).
Sets the authentication information according to the returned lambda.

- `toAuthenticateUsingHeader()` - authenticates requests based on the header with the supplied name.
Takes a lambda that processes the header (if present).
Sets the authentication information according to the returned lambda.

- `toAuthenticateUsingQueryParameter()` - authenticates requests based on the query parameter with the supplied name.
Takes a lambda that processes the query parameter (if present).
Sets the authentication information according to the returned lambda.

- `toAuthenticateUsingPathParameter()` - authenticates requests based on the path parameter with the supplied name.
Takes a lambda that processes the path parameter (if present).
Sets the authentication information according to the returned lambda.

#### Configuration options

- `notFailingOnMissingAuthentication()` - allows unauthenticated requests to not be rejected.

- `notFailingOnMissingAuthenticationForRequestsThat()` - allows unauthenticated requests that pass the supplied filter to not be rejected.

- `notFailingOnMissingAuthenticationForRequestsTo()` - allows unauthenticated requests whose route matches the supplied route specification (may contain wildcards) to not be rejected.

- `failingOnMissingAuthenticationOnlyForRequestsThat` - only rejects unauthenticated requests that pass the supplied filter.

- `failingOnMissingAuthenticationOnlyForRequestsTo()` - only rejects unauthenticated requests whose route matches the supplied route specification.

- `rejectingUnauthorizedRequestsUsing()` - calls the supplied handler on rejected requests.

- `onlyRequestsThat()` - applies the authenticator only to requests that pass the supplied filter.

- `onlyRequestsTo()` - applies the authenticator only to requests whose route matches the supplied route specification (may contain wildcards).

- `exceptRequestsThat()` - does not apply the authenticator to requests that pass the supplied filter.

- `exceptRequestsTo()` - does not apply the authenticator to requests whose route matches the supplied route specification (may contain wildcards).

- `beforeBodyProcessing()` - calls the authenticator before body processing (i.e. unmarshalling, map generation, etc.) takes place.

- `afterBodyProcessing()` - calls the authenticator after body processing (i.e. unmarshalling, map generation, etc.) takes place.


### Authorization
#### Available configurators

- `toAuthorizeRequestsUsing()` - authorizes requests with a lambda that gets the authentication information and the whole `HttpRequest`.
Rejects unauthorized requests by throwing a `NotAuthorizedException`. 

- `toAuthorizeAllAuthenticatedRequests()` - rejects all requests that are not authenticated (i.e. have an empty authentication information).
Rejects unauthorized requests by throwing a `NotAuthorizedException`.

#### Configuration options

- `rejectingUnauthorizedRequestsUsing()` - calls the supplied handler on rejected requests.

- `onlyRequestsThat()` - applies the authorizer only to requests that pass the supplied filter.

- `onlyRequestsTo()` - applies the authorizer only to requests whose route matches the supplied route specification (may contain wildcards).

- `exceptRequestsThat()` - does not apply the authorizer to requests that pass the supplied filter.

- `exceptRequestsTo()` - does not apply the authorizer to requests whose route matches the supplied route specification (may contain wildcards).

- `beforeBodyProcessing()` - calls the authorizer before body processing (i.e. unmarshalling, map generation, etc.) takes place.

- `afterBodyProcessing()` - calls the authorizer after body processing (i.e. unmarshalling, map generation, etc.) takes place.

### Request filters
Request filters behave the same as authorizers in that they reject requests based on certain criteria.
They offer a way to filter requests that might be deemed unsafe.

#### Available configurators
- `toFilterRequestsThat()` - filters requests based on the supplied `Filter` object.

#### Configuration options
- `rejectingFilteredRequestsUsing()` - calls the supplied handler on rejected requests.

- `onlyRequestsThat()` - applies the filter only to requests that pass the supplied filter.

- `onlyRequestsTo()` - applies the filter only to requests whose route matches the supplied route specification (may contain wildcards).

- `exceptRequestsThat()` - does not apply the filter to requests that pass the supplied filter.

- `exceptRequestsTo()` - does not apply the filter to requests whose route matches the supplied route specification (may contain wildcards).

- `beforeBodyProcessing()` - calls the filter before body processing (i.e. unmarshalling, map generation, etc.) takes place.

- `afterBodyProcessing()` - calls the filter after body processing (i.e. unmarshalling, map generation, etc.) takes place.

