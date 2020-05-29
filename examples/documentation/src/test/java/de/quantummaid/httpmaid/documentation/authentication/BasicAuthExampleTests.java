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
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;
import static de.quantummaid.httpmaid.documentation.support.Deployer.test;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthorizeRequestsUsing;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toDoBasicAuthWith;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;

public final class BasicAuthExampleTests {

    @Test
    public void step1() {
        //Showcase start basicAuthStep1
        final HttpMaid httpMaid = anHttpMaid()
                .get("/normal", (request, response) -> response.setBody("The normal section"))
                .get("/admin", (request, response) -> response.setBody("The admin section"))
                .build();
        //Showcase end basicAuthStep1
        test(httpMaid, client -> {
            Deployer.assertGet("/normal", "The normal section", client);
            Deployer.assertGet("/admin", "The admin section", client);
        });
    }

    @Test
    public void step2() {
        //Showcase start basicAuthStep2
        final UserDatabase userDatabase = new InMemoryUserDatabase();
        final HttpMaid httpMaid = anHttpMaid()
                .get("/normal", (request, response) -> response.setBody("The normal section"))
                .get("/admin", (request, response) -> response.setBody("The admin section"))
                .configured(toDoBasicAuthWith(userDatabase::authenticate).withMessage("Hello, please authenticate!"))
                .build();
        //Showcase end basicAuthStep2
        test(httpMaid, client -> {
            assertUnauthenticated("/normal", client);
            assertUnauthenticated("/admin", client);
            assertWrongCredentials("/normal", client);
            assertWrongCredentials("/admin", client);

            assertAuthenticated("/normal", "joe", "qrpk4L?>L(DBa[mN", "The normal section", client);
            assertAuthenticated("/admin", "joe", "qrpk4L?>L(DBa[mN", "The admin section", client);

            assertAuthenticated("/normal", "jack", "*eG)r@;{'4g'cM?3", "The normal section", client);
            assertAuthenticated("/admin", "jack", "*eG)r@;{'4g'cM?3", "The admin section", client);
        });
    }

    @Test
    public void step3() {
        //Showcase start basicAuthStep3
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
        //Showcase end basicAuthStep3
        test(httpMaid, client -> {
            assertUnauthenticated("/normal", client);
            assertUnauthenticated("/admin", client);
            assertWrongCredentials("/normal", client);
            assertWrongCredentials("/admin", client);

            assertAuthenticated("/normal", "joe", "qrpk4L?>L(DBa[mN", "The normal section", client);
            assertUnauthorized("/admin", "joe", "qrpk4L?>L(DBa[mN", "", client);

            assertAuthenticated("/normal", "jack", "*eG)r@;{'4g'cM?3", "The normal section", client);
            assertAuthenticated("/admin", "jack", "*eG)r@;{'4g'cM?3", "The admin section", client);
        });
    }

    @Test
    public void full() {
        //Showcase start basicAuthFull
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
        //Showcase end basicAuthFull
        test(httpMaid, client -> {
            assertUnauthenticated("/normal", client);
            assertUnauthenticated("/admin", client);
            assertWrongCredentials("/normal", client);
            assertWrongCredentials("/admin", client);

            assertAuthenticated("/normal", "joe", "qrpk4L?>L(DBa[mN", "The normal section", client);
            assertUnauthorized("/admin", "joe", "qrpk4L?>L(DBa[mN", "Please login as an administrator.", client);

            assertAuthenticated("/normal", "jack", "*eG)r@;{'4g'cM?3", "The normal section", client);
            assertAuthenticated("/admin", "jack", "*eG)r@;{'4g'cM?3", "The admin section", client);
        });
    }

    private static void assertUnauthenticated(final String path, final HttpMaidClient client) {
        final SimpleHttpResponseObject response = client.issue(aGetRequestToThePath(path));
        assertThat(response.getStatusCode(), is(401));
        assertThat(response.getBody(), is(""));
        assertThat(response.getHeaders(), hasEntry("www-authenticate", "Basic realm=\"Hello, please authenticate!\""));
    }

    private static void assertWrongCredentials(final String path, final HttpMaidClient client) {
        final SimpleHttpResponseObject response = authenticatedRequest(path, "foo", "bar", client);
        assertThat(response.getStatusCode(), is(401));
        assertThat(response.getBody(), is(""));
        assertThat(response.getHeaders(), hasEntry("www-authenticate", "Basic realm=\"Hello, please authenticate!\""));
    }

    private static void assertAuthenticated(final String path,
                                            final String username,
                                            final String password,
                                            final String response,
                                            final HttpMaidClient client) {
        final SimpleHttpResponseObject responseObject = authenticatedRequest(path, username, password, client);
        assertThat(responseObject.getStatusCode(), is(200));
        assertThat(responseObject.getBody(), is(response));
        assertThat(responseObject.getHeaders(), not((hasKey("www-authenticate"))));
    }

    private static void assertUnauthorized(final String path,
                                           final String username,
                                           final String password,
                                           final String response,
                                           final HttpMaidClient client) {
        final SimpleHttpResponseObject responseObject = authenticatedRequest(path, username, password, client);
        assertThat(responseObject.getStatusCode(), is(401));
        assertThat(responseObject.getBody(), is(response));
        assertThat(responseObject.getHeaders(), not((hasKey("www-authenticate"))));
    }

    private static SimpleHttpResponseObject authenticatedRequest(final String path,
                                                                 final String username,
                                                                 final String password,
                                                                 final HttpMaidClient client) {
        return client.issue(
                aGetRequestToThePath(path).withHeader("Authorization", encodeAsAuthorizationHeader(username, password))
        );
    }

    private static String encodeAsAuthorizationHeader(final String username, final String password) {
        final String unencoded = format("%s:%s", username, password);
        final String encoded = getEncoder().encodeToString(unencoded.getBytes(UTF_8));
        return format("Basic %s", encoded);
    }
}
