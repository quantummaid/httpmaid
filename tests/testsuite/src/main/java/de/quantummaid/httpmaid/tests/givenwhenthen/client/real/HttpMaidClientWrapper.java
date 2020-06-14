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

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.client.body.multipart.Part;
import de.quantummaid.httpmaid.client.clientbuilder.PortStage;
import de.quantummaid.httpmaid.client.issuer.real.Protocol;
import de.quantummaid.httpmaid.client.websocket.Websocket;
import de.quantummaid.httpmaid.tests.givenwhenthen.Headers;
import de.quantummaid.httpmaid.tests.givenwhenthen.builders.MultipartElement;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientWrapper;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.WrappedWebsocket;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.ApiBaseUrl;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static de.quantummaid.httpmaid.client.HttpClientRequest.aRequest;
import static de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientBypassingRequestsDirectlyTo;
import static de.quantummaid.httpmaid.client.body.multipart.Part.aPartWithTheControlName;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse.httpClientResponse;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMaidClientWrapper implements HttpClientWrapper {
    private final HttpMaidClient httpClient;
    private final HttpMaidClient websocketClient;

    static HttpClientWrapper realHttpMaidClientWithConnectionReuseWrapper(final Deployment deployment) {
        return httpMaidClient(HttpMaidClient::aHttpMaidClientThatReusesConnectionsForTheHost, deployment);
    }

    static HttpClientWrapper realHttpMaidClientWrapper(final Deployment deployment) {
        return httpMaidClient(HttpMaidClient::aHttpMaidClientForTheHost, deployment);
    }

    private static HttpClientWrapper httpMaidClient(final Function<String, PortStage> builderEntry,
                                                    final Deployment deployment) {
        final HttpMaidClient httpClient = deployment.httpBaseUrl()
                .map(url -> httpClient(builderEntry, url))
                .orElse(null);
        final HttpMaidClient websocketClient = deployment.webSocketBaseUrl()
                .map(url -> httpClient(builderEntry, url))
                .orElse(null);
        return new HttpMaidClientWrapper(httpClient, websocketClient);
    }

    private static HttpMaidClient httpClient(final Function<String, PortStage> builderEntry,
                                             final ApiBaseUrl baseUrl) {
        final Protocol protocol = Protocol.parse(baseUrl.transportProtocol().toUpperCase());
        return builderEntry.apply(baseUrl.hostName)
                .withThePort(baseUrl.port)
                .viaTheProtocol(protocol)
                .withBasePath(baseUrl.basePath)
                .build();
    }

    static HttpClientWrapper bypassingHttpMaidClientWrapper(final Deployment deployment) {
        final HttpMaid httpMaid = deployment.httpMaid()
                .orElseThrow(() -> new UnsupportedOperationException("Not a bypassing deployment: " + deployment));
        final HttpMaidClient client = aHttpMaidClientBypassingRequestsDirectlyTo(httpMaid).build();
        return new HttpMaidClientWrapper(client, client);
    }

    @Override
    public HttpClientResponse issueRequestWithoutBody(final String path,
                                                      final String method,
                                                      final Headers headers) {
        return issueRequest(path, method, headers, builder -> {
        });
    }

    @Override
    public HttpClientResponse issueRequestWithStringBody(final String path,
                                                         final String method,
                                                         final Headers headers,
                                                         final String body) {
        return issueRequest(path, method, headers, bodyStage -> bodyStage.withTheBody(body));
    }

    @Override
    public HttpClientResponse issueRequestWithMultipartBody(final String path,
                                                            final String method,
                                                            final Headers headers,
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
                                            final Headers headers,
                                            final Consumer<HttpClientRequestBuilder<SimpleHttpResponseObject>> bodyAppender) {
        if (httpClient == null) {
            throw new UnsupportedOperationException("There is no http deployment to connect to. " +
                    "Probably the endpoint does not support http requests.");
        }
        final HttpClientRequestBuilder<SimpleHttpResponseObject> requestBuilder = aRequest(method, path);
        bodyAppender.accept(requestBuilder);
        headers.forEach(requestBuilder::withHeader);
        final SimpleHttpResponseObject response = this.httpClient.issue(requestBuilder);
        return httpClientResponse(response.getStatusCode(), response.getHeaders(), response.getBody());
    }

    @Override
    public WrappedWebsocket openWebsocket(final Consumer<String> responseHandler,
                                          final Map<String, String> queryParameters,
                                          final Map<String, List<String>> headers) {
        if (websocketClient == null) {
            throw new UnsupportedOperationException("There is no websocket deployment to connect to. " +
                    "Probably the endpoint does not support websockets.");
        }
        final Websocket websocket = websocketClient.openWebsocket(responseHandler::accept, queryParameters, headers);
        return WrappedWebsocket.wrappedWebsocket(websocket::send, websocket);
    }

    @Override
    public void close() {
        if (httpClient != null) {
            httpClient.close();
        }
        if (websocketClient != null) {
            websocketClient.close();
        }
    }
}
