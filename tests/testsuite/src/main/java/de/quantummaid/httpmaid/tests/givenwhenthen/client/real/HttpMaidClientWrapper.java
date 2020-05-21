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

package de.quantummaid.httpmaid.tests.givenwhenthen.client.real;

import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.client.body.multipart.Part;
import de.quantummaid.httpmaid.client.issuer.real.Protocol;
import de.quantummaid.httpmaid.client.websocket.Websocket;
import de.quantummaid.httpmaid.tests.givenwhenthen.builders.MultipartElement;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientWrapper;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.WrappedWebsocket;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.quantummaid.httpmaid.client.HttpClientRequest.aRequest;
import static de.quantummaid.httpmaid.client.HttpMaidClient.*;
import static de.quantummaid.httpmaid.client.body.multipart.Part.aPartWithTheControlName;
import static de.quantummaid.httpmaid.client.issuer.real.Protocol.valueOf;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse.httpClientResponse;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMaidClientWrapper implements HttpClientWrapper {
    private final HttpMaidClient httpClient;
    private final HttpMaidClient websocketClient;

    static HttpClientWrapper realHttpMaidClientWithConnectionReuseWrapper(final Deployment deployment) {
        final Protocol protocol = valueOf(deployment.protocol().toUpperCase());
        final HttpMaidClient httpClient = aHttpMaidClientThatReusesConnectionsForTheHost(deployment.httpHostname())
                .withThePort(deployment.httpPort())
                .viaTheProtocol(protocol)
                .withBasePath(deployment.httpBasePath())
                .build();
        final HttpMaidClient websocketClient = aHttpMaidClientThatReusesConnectionsForTheHost(deployment.websocketHostname())
                .withThePort(deployment.websocketPort())
                .viaTheProtocol(protocol)
                .withBasePath(deployment.websocketBasePath())
                .build();
        return new HttpMaidClientWrapper(httpClient, websocketClient);
    }

    static HttpClientWrapper realHttpMaidClientWrapper(final Deployment deployment) {
        final Protocol protocol = valueOf(deployment.protocol().toUpperCase());
        final HttpMaidClient httpClient = aHttpMaidClientForTheHost(deployment.httpHostname())
                .withThePort(deployment.httpPort())
                .viaTheProtocol(protocol)
                .withBasePath(deployment.httpBasePath())
                .build();
        final HttpMaidClient websocketClient = aHttpMaidClientForTheHost(deployment.websocketHostname())
                .withThePort(deployment.websocketPort())
                .viaTheProtocol(protocol)
                .withBasePath(deployment.websocketBasePath())
                .build();
        return new HttpMaidClientWrapper(httpClient, websocketClient);
    }

    static HttpClientWrapper bypassingHttpMaidClientWrapper(final Deployment deployment) {
        final HttpMaidClient client = aHttpMaidClientBypassingRequestsDirectlyTo(deployment.httpMaid())
                .build();
        return new HttpMaidClientWrapper(client, client);
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
        final SimpleHttpResponseObject response = this.httpClient.issue(requestBuilder);
        return httpClientResponse(response.getStatusCode(), response.getHeaders(), response.getBody());
    }

    @Override
    public void openWebsocketAndSendMessage(final Consumer<String> responseHandler,
                                            final String message,
                                            final Map<String, String> queryParameters,
                                            final Map<String, List<String>> headers) {
        final Websocket websocket = websocketClient.openWebsocket(responseHandler::accept, queryParameters, headers);
        websocket.send(message);
    }

    @Override
    public WrappedWebsocket openWebsocket(final Consumer<String> responseHandler,
                                          final Map<String, String> queryParameters,
                                          final Map<String, List<String>> headers) {
        final Websocket websocket = websocketClient.openWebsocket(responseHandler::accept, queryParameters, headers);
        return WrappedWebsocket.wrappedWebsocket(websocket::send, websocket);
    }

    @Override
    public void close() {
        httpClient.close();
        websocketClient.close();
    }
}
