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

import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.tests.givenwhenthen.JsonNormalizer.normalizeJsonToMap;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Then {
    private final HttpClientResponse response;
    private final Throwable initializationException;
    private final TestLogger logger;

    static Then then(final HttpClientResponse response,
                     final Throwable initializationException,
                     final TestLogger logger) {
        return new Then(response, initializationException, logger);
    }

    public Then anExceptionHasBeenThrownDuringInitializationWithAMessageContaining(final String expectedMessage) {
        initializationException.printStackTrace();
        final String actualMessage = initializationException.getMessage();
        assertThat(actualMessage, containsString(expectedMessage));
        return this;
    }

    public Then theStatusCodeWas(final int expectedStatusCode) {
        final int actualStatusCode = response.getStatusCode();
        assertThat(actualStatusCode, is(expectedStatusCode));
        return this;
    }

    public Then theResponseContentTypeWas(final String expectedContentType) {
        return theReponseContainsTheHeader("Content-Type", expectedContentType);
    }

    public Then theReponseContainsTheHeader(final String key, final String value) {
        final Map<String, String> headers = response.getHeaders();
        final Map<String, String> normalizedHeaders = new HashMap<>();
        headers.forEach((k, v) -> normalizedHeaders.put(k.toLowerCase(), v));
        final String normalizedKey = key.toLowerCase();
        assertThat(normalizedHeaders.keySet(), hasItem(normalizedKey));
        final String actualValue = normalizedHeaders.get(normalizedKey);
        assertThat(actualValue, is(value));
        return this;
    }

    public Then theResponseBodyWas(final String expectedResponseBody) {
        final String actualResponseBody = response.getBody();
        assertThat(actualResponseBody, is(expectedResponseBody));
        return this;
    }

    public Then theResponseBodyContains(final String expectedResponseBody) {
        final String actualResponseBody = response.getBody();
        assertThat(actualResponseBody, containsString(expectedResponseBody));
        return this;
    }

    public Then theLogOutputStartedWith(final String expectedPrefix) {
        final String logContent = logger.logContent();
        assertThat(logContent, startsWith(expectedPrefix));
        return this;
    }

    public Then theJsonResponseEquals(final String expectedJson) {
        final Map<String, Object> normalizedExpected = normalizeJsonToMap(expectedJson);
        final String actualResponseBody = response.getBody();
        final Map<String, Object> normalizedActual = normalizeJsonToMap(actualResponseBody);
        assertThat(normalizedActual, is(normalizedExpected));
        return this;
    }
}
