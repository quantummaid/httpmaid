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

import de.quantummaid.httpmaid.tests.givenwhenthen.builders.*;
import de.quantummaid.httpmaid.tests.givenwhenthen.checkpoints.Checkpoints;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientRequest;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientWrapper;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.WrappedWebsocket;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.tests.givenwhenthen.Headers.emptyHeaders;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientRequest.httpClientRequest;
import static java.lang.String.format;
import static java.util.Arrays.stream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class When implements FirstWhenStage, MethodBuilder, BodyBuilder, HeaderBuilder {
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
    public Then httpMaidIsInitialized() {
        return Then.then(testData);
    }

    @Override
    public Then aWebsocketIsConnected(final Map<String, String> queryParameters,
                                      final Map<String, List<String>> headers) {
        final Checkpoints checkpoints = testData.getCheckpoints();
        final WrappedWebsocket websocket = testData.getClientWrapper().openWebsocket(checkpoints::visitCheckpoint, queryParameters, headers);
        testData.setWebsocket(websocket);
        return Then.then(testData);
    }

    @Override
    public Then aWebsocketMessageIsSent(final String message) {
        final WrappedWebsocket websocket = testData.getWebsocket();
        ResourcesTracker.addResource(websocket);
        websocket.send(message);
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
