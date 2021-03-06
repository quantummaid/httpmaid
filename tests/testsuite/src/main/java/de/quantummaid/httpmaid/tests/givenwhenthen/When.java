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

package de.quantummaid.httpmaid.tests.givenwhenthen;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.RuntimeInformation;
import de.quantummaid.httpmaid.client.HttpMaidClientException;
import de.quantummaid.httpmaid.tests.givenwhenthen.builders.*;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientRequest;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientWrapper;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.WrappedWebsocket;
import de.quantummaid.httpmaid.tests.givenwhenthen.websockets.ManagedWebsocket;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.tests.givenwhenthen.Headers.emptyHeaders;
import static de.quantummaid.httpmaid.tests.givenwhenthen.Poller.sleep;
import static de.quantummaid.httpmaid.tests.givenwhenthen.WebsocketTestClientConnectException.websocketTestClientConnectException;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientRequest.httpClientRequest;
import static de.quantummaid.httpmaid.tests.givenwhenthen.websockets.WebsocketStatus.CLOSED;
import static java.lang.String.format;
import static java.util.Arrays.stream;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class When implements FirstWhenStage, MethodBuilder, BodyBuilder, HeaderBuilder {
    private static final int WAIT_TIME_BASE = 1000;

    private String method;
    private String path;
    private List<QueryStringParameter> queryStringParameters = new ArrayList<>();
    private final Headers headers = emptyHeaders();
    private Object body;
    private final TestData testData;

    static When when(final TestData testData) {
        return new When(testData);
    }

    @Override
    public Then running(final Runnable runnable) {
        runnable.run();
        return Then.then(testData);
    }

    @Override
    public Then aWebsocketIsConnected(final Map<String, List<String>> queryParameters,
                                      final Map<String, List<String>> headers,
                                      final int maxConnectionAttempts) {
        int tryNumber = 0;
        int waitTime = WAIT_TIME_BASE;
        while (true) {
            try {
                return tryToConnectWebsocket(queryParameters, headers);
            } catch (final HttpMaidClientException e) {
                log.warn("connect attempt {} failed", tryNumber, e);
                if (tryNumber + 1 == maxConnectionAttempts) {
                    throw websocketTestClientConnectException(maxConnectionAttempts, e);
                } else {
                    log.warn("retrying in {} ms", waitTime);
                    sleep(waitTime);
                    tryNumber = tryNumber + 1;
                    waitTime = waitTime * 2;
                }
            }
        }
    }

    private Then tryToConnectWebsocket(final Map<String, List<String>> queryParameters,
                                       final Map<String, List<String>> headers) {
        final ManagedWebsocket managedWebsocket = ManagedWebsocket.managedWebsocket();
        final WrappedWebsocket websocket = testData.getClientWrapper().openWebsocket(
                managedWebsocket::addMessage,
                () -> managedWebsocket.setStatus(CLOSED),
                queryParameters,
                headers
        );
        managedWebsocket.setWebsocket(websocket);
        testData.getWebsockets().addWebsocket(managedWebsocket);
        testData.getTestEnvironment().addResourceToBeClosed(websocket);
        return Then.then(testData);
    }

    @Override
    public Then aWebsocketMessageIsSent(final String message) {
        final WrappedWebsocket websocket = testData.getWebsockets().latestWebsocket();
        websocket.send(message);
        return Then.then(testData);
    }

    @Override
    public Then theLastWebsocketIsDisconnected() {
        final WrappedWebsocket websocket = testData.getWebsockets().latestWebsocket();
        try {
            websocket.close();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        return Then.then(testData);
    }

    @Override
    public Then allWebsocketsAreDisconnected() {
        testData.getWebsockets().all().forEach(websocket -> {
            final WrappedWebsocket websocketWebsocket = websocket.getWebsocket();
            try {
                websocketWebsocket.close();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        return Then.then(testData);
    }

    @Override
    public Then theRuntimeDataIsQueriedUntilTheNumberOfWebsocketsBecomes(final long expectedNumberOfWebsockets) {
        final int maxNumberOfTries = 60;
        final int sleepTimeInMilliseconds = 1000;
        Poller.pollWithTimeout(maxNumberOfTries, sleepTimeInMilliseconds, () -> {
            theRuntimeDataIsQueried();
            final RuntimeInformation runtimeInformation = testData.getRuntimeInformation();
            final long actualNumberOfWebsockets = runtimeInformation.numberOfConnectedWebsockets();
            return actualNumberOfWebsockets == expectedNumberOfWebsockets;
        });
        return Then.then(testData);
    }

    @Override
    public Then theRuntimeDataIsQueried() {
        final HttpMaid httpMaid = testData.getHttpMaid();
        final RuntimeInformation runtimeInformation = httpMaid.queryRuntimeInformation();
        testData.setRuntimeInformation(runtimeInformation);
        return Then.then(testData);
    }

    @Override
    public MethodBuilder aRequestToThePath(final String path) {
        this.path = path;
        return this;
    }

    @Override
    public BodyBuilder viaTheGetMethod() {
        method = "GET";
        return this;
    }

    @Override
    public BodyBuilder viaThePostMethod() {
        method = "POST";
        return this;
    }

    @Override
    public BodyBuilder viaThePutMethod() {
        method = "PUT";
        return this;
    }

    @Override
    public BodyBuilder viaTheDeleteMethod() {
        method = "DELETE";
        return this;
    }

    @Override
    public BodyBuilder viaTheOptionsMethod() {
        method = "OPTIONS";
        return this;
    }

    @Override
    public HeaderBuilder withAnEmptyBody() {
        this.body = null;
        return this;
    }

    @Override
    public HeaderBuilder withTheBody(final String body) {
        this.body = body;
        return this;
    }

    @Override
    public HeaderBuilder withTheMultipartBody(final MultipartBuilder multipartBuilder) {
        body = multipartBuilder.getElements();
        return this;
    }

    @Override
    public HeaderBuilder withQueryStringParameter(final String name, final String value) {
        queryStringParameters.add(QueryStringParameter.queryStringParameter(name, value));
        return this;
    }

    @Override
    public HeaderBuilder withHeaderOccuringMultipleTimesHavingDistinctValue(final String key, final String... values) {
        if (headers.containsName(key)) {
            throw new IllegalArgumentException(format("Header key '%s' is already present in %s", key, headers));
        }
        stream(values).forEach(value -> headers.add(key, value));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Then isIssued() {
        try (HttpClientWrapper clientWrapper = testData.getClientWrapper()) {
            final HttpClientRequest request = httpClientRequest(path, method, queryStringParameters, headers);
            final HttpClientResponse response;
            if (body == null) {
                response = clientWrapper.issueRequestWithoutBody(request);
            } else if (body instanceof String) {
                response = clientWrapper.issueRequestWithStringBody(request, (String) body);
            } else {
                response = clientWrapper.issueRequestWithMultipartBody(request, (List<MultipartElement>) body);
            }
            testData.setResponse(response);
            return Then.then(testData);
        }
    }
}
