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

package de.quantummaid.httpmaid.documentation.react;

import de.quantummaid.httpmaid.HttpMaid;
import com.google.gson.Gson;
import de.quantummaid.mapmaid.MapMaid;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.cors.CorsConfigurators.toActivateCORSWithoutValidatingTheOrigin;
import static de.quantummaid.httpmaid.documentation.react.LoginException.loginException;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static de.quantummaid.httpmaid.http.Http.StatusCodes.BAD_REQUEST;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurator.toUseMapMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthenticateUsingOAuth2BearerToken;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthorizeAllAuthenticatedRequests;
import static de.quantummaid.mapmaid.MapMaid.aMapMaid;
import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.parser;
import static io.jsonwebtoken.security.Keys.secretKeyFor;
import static java.util.Objects.isNull;

public final class ReactExample {
    private static final Map<String, String> userDatabase = Map.of("joe", hashPassword("foo"));

    private static final Key key = secretKeyFor(SignatureAlgorithm.HS256);
    private static final JwtParser jwtParser = parser().setSigningKey(key);

    public static void main(String[] args) {
        final Gson gson = new Gson();

        final MapMaid mapMaid = aMapMaid()
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .build();

        final HttpMaid httpMaid = anHttpMaid()
                .post("/login", (request, response) -> {
                    final Map<String, Object> bodyMap = request.bodyMap();
                    final String username = (String) bodyMap.get("username");
                    final String password = (String) bodyMap.get("password");

                    checkCredentials(username, password);

                    final String jws = builder()
                            .setSubject(username)
                            .signWith(key).compact();

                    response.setBody(Map.of("token", jws));
                })
                .get("/dashboard", (request, response) -> response.setBody(Map.of("message", new Date().toString())))
                .configured(toUseMapMaid(mapMaid))
                .configured(toActivateCORSWithoutValidatingTheOrigin())
                .configured(toAuthenticateUsingOAuth2BearerToken(ReactExample::checkJwt))
                .configured(toAuthorizeAllAuthenticatedRequests().exceptRequestsTo("/login"))
                .configured(toMapExceptionsOfType(LoginException.class, (exception, response) -> {
                    response.setBody(Map.of(
                            "errorType", "LOGIN",
                            "errorMessage", exception.getMessage()
                    ));
                    response.setStatus(BAD_REQUEST);
                }))
                .build();

        System.out.println("httpMaid = " + httpMaid.dumpChains());

        pureJavaEndpointFor(httpMaid).listeningOnThePort(1300);

    }

    private static Optional<String> checkJwt(final String token) {
        try {
            return Optional.ofNullable(jwtParser.parseClaimsJws(token).getBody().getSubject());
        } catch (final JwtException e) {
            return Optional.empty();
        }
    }

    private static void checkCredentials(final String username, final String password) {
        if (isNull(username) || username.isEmpty()) {
            throw loginException("username is empty");
        }
        if (isNull(password) || password.isEmpty()) {
            throw loginException("password is empty");
        }
        if (!userDatabase.containsKey(username)) {
            throw loginException("login failed");
        }
        final String hash = userDatabase.get(username);
        if (!hash.equals(hashPassword(password))) {
            throw loginException("login failed");
        }
    }

    private static String hashPassword(final String password) {
        return password;
    }
}
