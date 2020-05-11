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

package de.quantummaid.httpmaid.documentation.support;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.endpoint.purejavaendpoint.PureJavaEndpoint;

import java.util.function.Consumer;

import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aPostRequestToThePath;
import static de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientForTheHost;
import static de.quantummaid.httpmaid.documentation.support.FreePortPool.freePort;
import static de.quantummaid.httpmaid.endpoint.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public final class Deployer {

    private Deployer() {
    }

    public static void test(final HttpMaid httpMaid, final Consumer<HttpMaidClient> tester) {
        final int port = freePort();
        try (final PureJavaEndpoint pureJavaEndpoint = pureJavaEndpointFor(httpMaid).listeningOnThePort(port)) {
            assertThat(pureJavaEndpoint, notNullValue());
            final HttpMaidClient client = aHttpMaidClientForTheHost("localhost")
                    .withThePort(port)
                    .viaHttp()
                    .build();
            tester.accept(client);
        }
    }

    public static void assertGet(final String route, final String expectedResponse, final HttpMaidClient client) {
        assertGet(route, expectedResponse, 200, client);
    }

    public static void assertGet(final String route,
                                 final String expectedResponse,
                                 final int expectedStatusCode,
                                 final HttpMaidClient client) {
        final SimpleHttpResponseObject response = client.issue(aGetRequestToThePath(route));
        final int statusCode = response.getStatusCode();
        assertThat(statusCode, is(expectedStatusCode));
        final String body = response.getBody();
        assertThat(body, is(expectedResponse));
    }

    public static void assertPost(final String route,
                                  final String content,
                                  final String expectedResponse,
                                  final HttpMaidClient client) {
        final HttpClientRequestBuilder<SimpleHttpResponseObject> requestBuilder = aPostRequestToThePath(route)
                .withTheBody(content);
        final SimpleHttpResponseObject response = client.issue(requestBuilder);
        final int statusCode = response.getStatusCode();
        assertThat(statusCode, is(200));
        final String body = response.getBody();
        assertThat(body, is(expectedResponse));
    }
}
