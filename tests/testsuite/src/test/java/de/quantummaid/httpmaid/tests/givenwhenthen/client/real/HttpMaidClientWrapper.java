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

package de.quantummaid.httpmaid.tests.givenwhenthen.client.real;

import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.client.body.multipart.Part;
import de.quantummaid.httpmaid.client.issuer.real.Protocol;
import de.quantummaid.httpmaid.tests.givenwhenthen.builders.MultipartElement;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientWrapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.quantummaid.httpmaid.client.HttpClientRequest.aRequest;
import static de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientBypassingRequestsDirectlyTo;
import static de.quantummaid.httpmaid.client.body.multipart.Part.aPartWithTheControlName;
import static de.quantummaid.httpmaid.client.issuer.real.Protocol.valueOf;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse.httpClientResponse;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMaidClientWrapper implements HttpClientWrapper {

    private final HttpMaidClient client;

    static HttpClientWrapper realHttpMaidClientWithConnectionReueWrapper(final Deployment deployment) {
        final Protocol protocol = valueOf(deployment.protocol().toUpperCase());
        final HttpMaidClient client = HttpMaidClient.aHttpMaidClientThatReusesConnectionsForTheHost(deployment.hostname())
                .withThePort(deployment.port())
                .viaTheProtocol(protocol)
                .withBasePath(deployment.basePath())
                .build();
        return new HttpMaidClientWrapper(client);
    }

    static HttpClientWrapper realHttpMaidClientWrapper(final Deployment deployment) {
        final Protocol protocol = valueOf(deployment.protocol().toUpperCase());
        final HttpMaidClient client = HttpMaidClient.aHttpMaidClientForTheHost(deployment.hostname())
                .withThePort(deployment.port())
                .viaTheProtocol(protocol)
                .withBasePath(deployment.basePath())
                .build();
        return new HttpMaidClientWrapper(client);
    }

    static HttpClientWrapper bypassingHttpMaidClientWrapper(final Deployment deployment) {
        final HttpMaidClient client = aHttpMaidClientBypassingRequestsDirectlyTo(deployment.httpMaid())
                .build();
        return new HttpMaidClientWrapper(client);
    }

    @Override
    public HttpClientResponse issueRequestWithoutBody(final String path,
                                                      final String method,
                                                      final Map<String, String> headers) {
        return issueRequest(path, method, headers, builder -> {
        });
    }

    @Override
    public HttpClientResponse issueRequestWithStringBody(final String path,
                                                         final String method,
                                                         final Map<String, String> headers,
                                                         final String body) {
        return issueRequest(path, method, headers, bodyStage -> bodyStage.withTheBody(body));
    }

    @Override
    public HttpClientResponse issueRequestWithMultipartBody(final String path,
                                                            final String method,
                                                            final Map<String, String> headers,
                                                            final List<MultipartElement> parts) {
        return issueRequest(path, method, headers, builder -> {
            final Part[] partsArray = parts.stream()
                    .map(part -> aPartWithTheControlName(part.controlName())
                            .withTheFileName(part.fileName().orElse(null))
                            .withTheContent(part.content()))
                    .toArray(Part[]::new);
            builder.withAMultipartBodyWithTheParts(partsArray);
        });
    }

    private HttpClientResponse issueRequest(final String path,
                                            final String method,
                                            final Map<String, String> headers,
                                            final Consumer<HttpClientRequestBuilder<SimpleHttpResponseObject>> bodyAppender) {
        final HttpClientRequestBuilder<SimpleHttpResponseObject> requestBuilder = aRequest(method, path);
        bodyAppender.accept(requestBuilder);
        headers.forEach(requestBuilder::withHeader);
        final SimpleHttpResponseObject response = this.client.issue(requestBuilder);
        return httpClientResponse(response.getStatusCode(), response.getHeaders(), response.getBody());
    }

    @Override
    public void close() {
        client.close();
    }
}