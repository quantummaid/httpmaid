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
import de.quantummaid.httpmaid.client.HttpClientRequest;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.documentation.support.Deployer.assertGet;
import static de.quantummaid.httpmaid.documentation.support.Deployer.test;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthorizeRequestsUsing;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toDoBasicAuthWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
            final int normalStatusCode = client.issue(HttpClientRequest.aGetRequestToThePath("/normal")).getStatusCode();
            assertThat(normalStatusCode, is(401));

            final int adminStatusCode = client.issue(HttpClientRequest.aGetRequestToThePath("/admin")).getStatusCode();
            assertThat(adminStatusCode, is(401));
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
            final int normalStatusCode = client.issue(HttpClientRequest.aGetRequestToThePath("/normal")).getStatusCode();
            assertThat(normalStatusCode, is(401));

            final int adminStatusCode = client.issue(HttpClientRequest.aGetRequestToThePath("/admin")).getStatusCode();
            assertThat(adminStatusCode, is(401));
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
            final int normalStatusCode = client.issue(HttpClientRequest.aGetRequestToThePath("/normal")).getStatusCode();
            assertThat(normalStatusCode, is(401));

            final int adminStatusCode = client.issue(HttpClientRequest.aGetRequestToThePath("/admin")).getStatusCode();
            assertThat(adminStatusCode, is(401));
        });
    }


}
