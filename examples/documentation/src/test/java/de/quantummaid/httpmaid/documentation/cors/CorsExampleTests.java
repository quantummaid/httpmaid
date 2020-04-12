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

package de.quantummaid.httpmaid.documentation.cors;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.anOptionsRequestToThePath;
import static de.quantummaid.httpmaid.cors.CorsConfigurators.toActivateCORSWithAllowedOrigins;
import static de.quantummaid.httpmaid.http.HttpRequestMethod.PUT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class CorsExampleTests {

    @Test
    public void corsExample() {
        //Showcase start cors
        final HttpMaid httpMaid = anHttpMaid()
                .put("/api", (request, response) -> response.setBody("Version 1.0"))
                .configured(toActivateCORSWithAllowedOrigins("frontend.example.org").withAllowedMethods(PUT))
                .build();
        //Showcase end cors

        Deployer.test(httpMaid, client -> {
            final SimpleHttpResponseObject responseObject = client.issue(
                    anOptionsRequestToThePath("/")
                            .withHeader("Origin", "frontend.example.org")
                            .withHeader("Access-Control-Request-Method", "PUT")
            );
            final Map<String, String> headers = responseObject.getHeaders();
            final String method = headers.get("access-control-allow-methods");
            assertThat(method, is("PUT"));
        });
    }
}
