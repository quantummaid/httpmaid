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
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientWrapper;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.WrappedWebsocket;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class When implements FirstWhenStage, MethodBuilder, BodyBuilder, HeaderBuilder {
    private String path;
    private String method;
    private final Map<String, String> headers = new HashMap<>();
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
    public HeaderBuilder withTheHeader(final String key, final String value) {
        headers.put(key, value);
        return this;
    }

    @Override
    public HeaderBuilder withContentType(final String contentType) {
        return withTheHeader("Content-Type", contentType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Then isIssued() {
        try (HttpClientWrapper clientWrapper = testData.getClientWrapper()) {
            final HttpClientResponse response;
            if (body == null) {
                response = clientWrapper.issueRequestWithoutBody(path, method, headers);
            } else if (body instanceof String) {
                response = clientWrapper.issueRequestWithStringBody(path, method, headers, (String) body);
            } else {
                response = clientWrapper.issueRequestWithMultipartBody(path, method, headers, (List<MultipartElement>) body);
            }
            testData.setResponse(response);
            return Then.then(testData);
        }
    }
}
