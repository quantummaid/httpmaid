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

package de.quantummaid.httpmaid.documentation.routing;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class RoutingExampleTests {

    @Test
    public void pathParametersExample() {
        //Showcase start pathParameters
        final HttpMaid httpMaid = anHttpMaid()
                .get("/items/<itemId>", (request, response) -> {
                    final String itemId = request.pathParameters().getPathParameter("itemId");
                    System.out.println("itemId = " + itemId);
                })
                .build();
        //Showcase end pathParameters

        Deployer.test(httpMaid, client -> Deployer.assertGet("/items/<foo>", "", client));
    }

    @Test
    public void methodsExample() {
        //Showcase start httpMethods
        final HttpMaid httpMaid = anHttpMaid()
                .get("/test", (request, response) -> System.out.println("This is a GET request"))
                .post("/test", (request, response) -> System.out.println("This is a POST request"))
                .put("/test", (request, response) -> System.out.println("This is a PUT request"))
                .delete("/test", (request, response) -> System.out.println("This is a DELETE request"))
                .build();
        //Showcase end httpMethods

        Deployer.test(httpMaid, client -> {
            assertMethod("GET", client);
            assertMethod("POST", client);
            assertMethod("PUT", client);
            assertMethod("DELETE", client);
        });
    }

    private void assertMethod(final String method, final HttpMaidClient client) {
        final SimpleHttpResponseObject response = client.issue(aRequest(method, "/test"));
        assertThat(response.getStatusCode(), is(200));
    }
}
