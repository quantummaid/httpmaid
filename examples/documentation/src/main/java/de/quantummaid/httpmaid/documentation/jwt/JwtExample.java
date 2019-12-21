/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.quantummaid.httpmaid.documentation.jwt;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.security.authorization.NotAuthorizedException;
import de.quantummaid.mapmaid.MapMaid;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;

import java.security.Key;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.toUseMapMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthenticateRequestsUsing;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthorizeRequestsUsing;
import static de.quantummaid.mapmaid.MapMaid.aMapMaid;
import static de.quantummaid.mapmaid.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe.urlEncodedMarshaller;
import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.parser;
import static io.jsonwebtoken.security.Keys.secretKeyFor;

public final class JwtExample {

    public static void main(final String[] args) {
        final Key key = secretKeyFor(SignatureAlgorithm.HS256);
        final JwtParser jwtParser = parser().setSigningKey(key);

        final Map<String, String> userDatabase = Map.of("joe", hashPassword("foo"));

        final MapMaid mapMaid = aMapMaid()
                .usingRecipe(urlEncodedMarshaller())
                .build();

        final HttpMaid httpMaid = anHttpMaid()
                .get("/", (request, response) -> response.setJavaResourceAsBody("start.html"))
                .get("/secret", (request, response) -> {
                    System.out.println("secret");
                    response.setBody("This is secret");
                })
                .get("/login", (request, response) -> response.setJavaResourceAsBody("login.html"))
                .get("/logout", (request, response) -> {
                    System.out.println("logout");
                    response.invalidateCookie("jwt");
                    response.redirectTo("/");
                })
                .post("/login", (request, response) -> {
                    final String username = (String) request.bodyMap().get("user");
                    final String password = (String) request.bodyMap().get("password");

                    if(!userDatabase.containsKey(username)) {
                        throw new LoginException();
                    }
                    final String hash = userDatabase.get(username);
                    if(!hash.equals(hashPassword(password))) {
                        throw new LoginException();
                    }

                    final String jws = builder()
                            .setSubject(username)
                            .signWith(key).compact();
                    response.setCookie("jwt", jws);
                    final String redirectionTarget = request.queryParameters()
                            .getOptionalQueryParameter("redirect")
                            .orElse("/");
                    response.redirectTo(redirectionTarget);
                })
                .configured(toAuthenticateRequestsUsing(request -> request.cookies().getOptionalCookie("jwt")
                        .map(token -> jwtParser.parseClaimsJws(token).getBody().getSubject())))
                .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> authenticationInformation.isPresent()).exceptRequestsTo("/login"))
                .configured(toMapExceptionsOfType(NotAuthorizedException.class, (exception, response) -> {
                    System.out.println("a");
                    response.invalidateCookie("jwt");
                    response.redirectTo("/login");
                }))
                .configured(toMapExceptionsOfType(SignatureException.class, (exception, response) -> {
                    System.out.println("b");
                    response.invalidateCookie("jwt");
                    response.redirectTo("/login");
                }))
                .configured(toMapExceptionsOfType(LoginException.class, (exception, response) -> {
                    System.out.println("c");
                    response.invalidateCookie("jwt");
                    response.redirectTo("/login");
                }))
                .configured(toUseMapMaid(mapMaid))
                .build();
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1338);
    }

    private static String hashPassword(final String password) {
        return password;
    }
}
