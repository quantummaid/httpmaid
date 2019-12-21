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

package de.quantummaid.httpmaid.documentation.xx_authentication;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.mapmaid.MapMaid;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.Key;
import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.toUseMapMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthenticateUsingCookie;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthorizeRequestsUsing;
import static de.quantummaid.mapmaid.MapMaid.aMapMaid;
import static de.quantummaid.mapmaid.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe.urlEncodedMarshaller;
import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.parser;
import static io.jsonwebtoken.security.Keys.secretKeyFor;

public final class CustomLoginExampleFull {

    public static void main(final String[] args) {
        //Showcase start customLoginFull
        final Key key = secretKeyFor(SignatureAlgorithm.HS256);
        final JwtParser jwtParser = parser().setSigningKey(key);

        final MapMaid mapMaid = aMapMaid()
                .usingRecipe(urlEncodedMarshaller())
                .build();
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
                .configured(toUseMapMaid(mapMaid))
                .build();
        //Showcase end customLoginFull
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
