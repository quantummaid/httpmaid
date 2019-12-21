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

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.toUseMapMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthorizeRequestsUsing;
import static de.quantummaid.mapmaid.MapMaid.aMapMaid;
import static de.quantummaid.mapmaid.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe.urlEncodedMarshaller;

public final class CustomLoginExampleStep1 {

    public static void main(final String[] args) {
        //Showcase start customLoginStep1
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
                .configured(toUseMapMaid(mapMaid))
                .build();
        //Showcase end customLoginStep1
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
