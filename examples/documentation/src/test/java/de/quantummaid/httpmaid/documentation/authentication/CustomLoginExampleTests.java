/*
 * Copyright (c) 2020 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid.documentation.authentication;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aPostRequestToThePath;
import static de.quantummaid.httpmaid.documentation.support.Deployer.test;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthenticateUsingCookie;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthorizeRequestsUsing;
import static de.quantummaid.httpmaid.usecases.UseCaseConfigurators.withMapperConfiguration;
import static de.quantummaid.mapmaid.builder.recipes.urlencoded.UrlEncodedMarshallerRecipe.urlEncodedMarshaller;
import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.parserBuilder;
import static io.jsonwebtoken.security.Keys.secretKeyFor;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class CustomLoginExampleTests {

    @Test
    public void step1() {
        //Showcase start customLoginStep1
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
        //Showcase end customLoginStep1

        test(httpMaid, client -> {
            final String login = client.issue(aGetRequestToThePath("/login").mappedToString());
            assertThat(login, containsString("Username:"));

            final String normal = client.issue(aGetRequestToThePath("/normal").mappedToString());
            assertThat(normal, is("The normal section"));

            final String admin = client.issue(aGetRequestToThePath("/admin").mappedToString());
            assertThat(admin, containsString("Please login as an administrator."));
        });
    }

    @Test
    public void step2() {
        //Showcase start customLoginFull
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
        //Showcase end customLoginFull

        test(httpMaid, client -> {
            final String login = client.issue(aGetRequestToThePath("/login").mappedToString());
            assertThat(login, containsString("Username:"));

            final String normal = client.issue(aGetRequestToThePath("/normal").mappedToString());
            assertThat(normal, is(""));

            final String admin = client.issue(aGetRequestToThePath("/admin").mappedToString());
            assertThat(admin, containsString(""));

            final SimpleHttpResponseObject normalLogin = client.issue(aPostRequestToThePath("/login")
                    .withTheBody("username=joe&password=qrpk4L?>L(DBa[mN")
                    .withContentType("application/x-www-form-urlencoded"));
            assertThat(normalLogin.getStatusCode(), is(200));
            final String normalJwtCookie = normalLogin.getSingleHeader("set-cookie");

            final String normalAsNormal = client.issue(aGetRequestToThePath("/normal").withHeader("Cookie", normalJwtCookie).mappedToString());
            assertThat(normalAsNormal, is("The normal section"));

            final String adminAsNormal = client.issue(aGetRequestToThePath("/admin").withHeader("Cookie", normalJwtCookie).mappedToString());
            assertThat(adminAsNormal, is("Please login as an administrator."));

            final SimpleHttpResponseObject adminLogin = client.issue(aPostRequestToThePath("/login")
                    .withTheBody("username=jack&password=*eG)r@;{'4g'cM?3")
                    .withContentType("application/x-www-form-urlencoded"));
            assertThat(adminLogin.getStatusCode(), is(200));
            final String adminJwtCookie = adminLogin.getSingleHeader("set-cookie");

            final String normalAsAdmin = client.issue(aGetRequestToThePath("/normal").withHeader("Cookie", adminJwtCookie).mappedToString());
            assertThat(normalAsAdmin, is("The normal section"));

            final String adminAsAdmin = client.issue(aGetRequestToThePath("/admin").withHeader("Cookie", adminJwtCookie).mappedToString());
            assertThat(adminAsAdmin, is("The admin section"));
        });
    }

}
