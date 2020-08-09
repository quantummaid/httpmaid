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

package de.quantummaid.httpmaid.tests.givenwhenthen.client.shitty;

import de.quantummaid.httpmaid.tests.givenwhenthen.builders.MultipartElement;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientRequest;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientWrapper;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.WrappedWebsocket;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.ApiBaseUrl;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import de.quantummaid.httpmaid.util.streams.Streams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse.httpClientResponse;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.WrappedWebsocket.wrappedWebsocket;
import static de.quantummaid.httpmaid.util.streams.Streams.inputStreamToString;
import static java.util.Arrays.stream;
import static org.apache.http.protocol.HttpProcessorBuilder.create;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShittyHttpClientWrapper implements HttpClientWrapper {
    private static final String MULTIPART_BOUNDARY = "abcdefggggg";
    private static final int BUFFER_SIZE = 8 * 1024;

    private final Deployment deployment;

    static HttpClientWrapper shittyHttpClientWrapper(final Deployment deployment) {
        return new ShittyHttpClientWrapper(deployment);
    }

    @Override
    public WrappedWebsocket openWebsocket(final Consumer<String> responseHandler,
                                          final Runnable closeHandler,
                                          final Map<String, List<String>> queryParameters,
                                          final Map<String, List<String>> headers) {
        final ApiBaseUrl url = deployment.webSocketBaseUrl()
                .orElseThrow(() -> new UnsupportedOperationException("Not a websocket deployment " + toString()));
        final ShittyWebsocketClient client = ShittyWebsocketClient.openWebsocket(
                url.toUrlString(),
                responseHandler,
                closeHandler,
                headers,
                queryParameters
        );
        return wrappedWebsocket(client::send, client);
    }

    @Override
    public HttpClientResponse issueRequestWithoutBody(final HttpClientRequest request) {
        return issueRequest(request, req -> {
        });
    }

    private HttpClientResponse issueRequest(final HttpClientRequest request,
                                            final Consumer<HttpEntityEnclosingRequest> bodyAppender) {
        final ApiBaseUrl baseUrl = deployment.httpBaseUrl()
                .orElseThrow(() -> new UnsupportedOperationException("Not an http deployment " + toString()));
        final String url = appendPathToUrl(baseUrl.toUrlString(), request.path);
        final String uri = buildUri(request, url);
        final HttpEntityEnclosingRequest request1 =
                new BasicHttpEntityEnclosingRequest(request.method, uri);
        request.headers.forEach(request1::addHeader);
        bodyAppender.accept(request1);
        try (DefaultBHttpClientConnection connection = new DefaultBHttpClientConnection(BUFFER_SIZE);
             Socket socket = createSocket(baseUrl)) {
            connection.bind(socket);
            final HttpProcessor httpProcessor = create()
                    .add(new RequestContent())
                    .add(new RequestTargetHost())
                    .build();
            final HttpCoreContext context = HttpCoreContext.create();
            context.setTargetHost(new HttpHost(baseUrl.hostName));
            httpProcessor.process(request1, context);
            final HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
            final HttpResponse response = httpexecutor.execute(request1, connection, context);
            final int statusCode = response.getStatusLine().getStatusCode();
            final Map<String, List<String>> responseHeaders = new HashMap<>();
            stream(response.getAllHeaders())
                    .forEach(header -> {
                        final String headerName = header.getName().toLowerCase();
                        final List<String> headerValues = responseHeaders.getOrDefault(headerName, new ArrayList<>());
                        headerValues.add(header.getValue());
                        responseHeaders.put(headerName, headerValues);
                    });
            final String responseBody = inputStreamToString(response.getEntity().getContent());
            return httpClientResponse(statusCode, responseHeaders, responseBody);
        } catch (final IOException | HttpException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String buildUri(final HttpClientRequest request, final String url) {
        try {
            final URIBuilder uriBuilder = new URIBuilder(url);
            request.queryStringParameters.forEach(
                    parameter -> uriBuilder.addParameter(parameter.name(), parameter.value()));
            return uriBuilder.build().toASCIIString();
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public HttpClientResponse issueRequestWithStringBody(final HttpClientRequest request, final String body) {
        return issueRequest(request, req -> {
            try {
                req.setEntity(new StringEntity(body));
            } catch (final UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public HttpClientResponse issueRequestWithMultipartBody(final HttpClientRequest request,
                                                            final List<MultipartElement> parts) {
        return issueRequest(request, builder -> {
            final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder
                    .create().setBoundary(MULTIPART_BOUNDARY);
            for (final MultipartElement part : parts) {
                if (part.fileName().isPresent()) {
                    final InputStream inputStream = Streams.stringToInputStream(part.content());
                    final InputStreamBody body = new InputStreamBody(inputStream, part.fileName().get());
                    multipartEntityBuilder.addPart(new FormBodyPart(part.controlName(), body));
                } else {
                    try {
                        final StringBody stringBody = new StringBody(part.content());
                        multipartEntityBuilder.addPart(new FormBodyPart(part.controlName(), stringBody));
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
            builder.setEntity(multipartEntityBuilder.build());
        });
    }

    private static String appendPathToUrl(final String url, final String path) {
        final String normalizedUrl;
        if (url.endsWith("/")) {
            normalizedUrl = url.substring(0, url.length() - 1);
        } else {
            normalizedUrl = url;
        }

        final String normalizedPath;
        if (path.startsWith("/")) {
            normalizedPath = path.substring(1);
        } else {
            normalizedPath = path;
        }
        return normalizedUrl + "/" + normalizedPath;
    }

    private static Socket createSocket(final ApiBaseUrl baseUrl) {
        try {
            return new Socket(baseUrl.hostName, baseUrl.port);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        // nothing to do
    }
}
