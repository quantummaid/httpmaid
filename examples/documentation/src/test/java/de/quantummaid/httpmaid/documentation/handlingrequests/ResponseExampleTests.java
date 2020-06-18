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

package de.quantummaid.httpmaid.documentation.handlingrequests;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.documentation.support.curl.Curl.parseFromCurlFile;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ResponseExampleTests {

    @Test
    public void responseBodyExample() {
        //Showcase start responseBody
        final HttpMaid httpMaid = anHttpMaid()
                .get("/test", (request, response) -> response.setBody("this is the body"))
                .build();
        //Showcase end responseBody

        Deployer.test(httpMaid, client ->
                Deployer.assertGet("/test", "this is the body", client));
    }

    @Test
    public void statusCodeExample() {
        //Showcase start statusCode
        final HttpMaid httpMaid = anHttpMaid()
                .get("/test", (request, response) -> response.setStatus(201))
                .build();
        //Showcase end statusCode

        Deployer.test(httpMaid, client -> {
            final HttpClientRequestBuilder<SimpleHttpResponseObject> request = parseFromCurlFile("statuscode.curl");

            final SimpleHttpResponseObject response = client.issue(request);
            final int statusCode = response.getStatusCode();
            assertThat(statusCode, is(201));
        });
    }

    @Test
    public void responseHeadersExample() {
        //Showcase start responseHeaders
        final HttpMaid httpMaid = anHttpMaid()
                .get("/test", (request, response) -> response.addHeader("name", "Bob"))
                .build();
        //Showcase end responseHeaders

        Deployer.test(httpMaid, client -> {
            final HttpClientRequestBuilder<SimpleHttpResponseObject> request = parseFromCurlFile("responseheader.curl");

            final SimpleHttpResponseObject response = client.issue(request);
            final int statusCode = response.getStatusCode();
            assertThat(statusCode, is(200));
            final String header = response.getSingleHeader("name");
            assertThat(header, is("Bob"));
        });
    }
}
