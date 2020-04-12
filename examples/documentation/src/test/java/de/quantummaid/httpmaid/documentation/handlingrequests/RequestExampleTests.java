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
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.HttpRequestMethod;
import de.quantummaid.httpmaid.http.PathParameters;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.path.Path;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.documentation.support.curl.Curl.parseFromCurlFile;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class RequestExampleTests {

    @Test
    public void pathExample() {
        //Showcase start pathExample
        final HttpMaid httpMaid = anHttpMaid()
                .get("/*", (request, response) -> {
                    final Path path = request.path();
                    System.out.println("path = " + path);
                })
                .build();
        //Showcase end pathExample

        Deployer.test(httpMaid, client ->
                Deployer.assertGet("/foo", "", client));
    }

    @Test
    public void methodExample() {
        //Showcase start requestPathMethod
        final HttpMaid httpMaid = anHttpMaid()
                .get("/", (request, response) -> {
                    final HttpRequestMethod method = request.method();
                    System.out.println("method = " + method);
                })
                .build();
        //Showcase end requestPathMethod
        Deployer.test(httpMaid, client ->
                Deployer.assertGet("/", "", client));
    }

    @Test
    public void requestBodyExample() {
        //Showcase start requestBody
        final HttpMaid httpMaid = anHttpMaid()
                .post("hello", (request, response) -> {
                    final String name = request.bodyString();
                    response.setBody("hi " + name + "!");
                })
                .build();
        //Showcase end requestBody

        Deployer.test(httpMaid, client -> {
            final HttpClientRequestBuilder<SimpleHttpResponseObject> request = parseFromCurlFile("body.curl");

            final SimpleHttpResponseObject response = client.issue(request);
            final int statusCode = response.getStatusCode();
            assertThat(statusCode, is(200));
            final String body = response.getBody();
            assertThat(body, is("hi bob!"));
        });
    }

    @Test
    public void queryParametersExample() {
        //Showcase start queryParameters
        final HttpMaid httpMaid = anHttpMaid()
                .get("/hello", (request, response) -> {
                    final QueryParameters queryParameters = request.queryParameters();
                    final String name = queryParameters.getQueryParameter("name");
                    response.setBody("hi " + name + "!");
                })
                .build();
        //Showcase end queryParameters

        Deployer.test(httpMaid, client ->
                Deployer.assertGet("/hello?name=foo", "hi foo!", client));
    }

    @Test
    public void pathParametersExample() {
        //Showcase start pathParameters2
        final HttpMaid httpMaid = anHttpMaid()
                .get("/hello/<name>", (request, response) -> {
                    final PathParameters pathParameters = request.pathParameters();
                    final String name = pathParameters.getPathParameter("name");
                    response.setBody("hi " + name + "!");
                })
                .build();
        //Showcase end pathParameters2

        Deployer.test(httpMaid, client ->
                Deployer.assertGet("/hello/foo", "hi foo!", client));
    }

    @Test
    public void requestHeadersExample() {
        //Showcase start requestHeaders
        final HttpMaid httpMaid = anHttpMaid()
                .get("/hello", (request, response) -> {
                    final Headers headers = request.headers();
                    final String name = headers.getHeader("name");
                    response.setBody("hi " + name + "!");
                })
                .build();
        //Showcase end requestHeaders

        Deployer.test(httpMaid, client -> {
            final HttpClientRequestBuilder<SimpleHttpResponseObject> request = parseFromCurlFile("requestheader.curl");

            final SimpleHttpResponseObject response = client.issue(request);
            final int statusCode = response.getStatusCode();
            assertThat(statusCode, is(200));
            final String body = response.getBody();
            assertThat(body, is("hi bob!"));
        });
    }
}
