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

import de.quantummaid.httpmaid.tests.givenwhenthen.builders.FirstWhenStage;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.tests.givenwhenthen.JsonNormalizer.normalizeJsonToMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Then {
    private final TestData testData;

    static Then then(final TestData testData) {
        return new Then(testData);
    }

    public FirstWhenStage andWhen() {
        return When.when(testData);
    }

    public Then anExceptionHasBeenThrownDuringInitializationWithAMessageContaining(final String expectedMessage) {
        final String actualMessage = testData.getInitializationException().getMessage();
        MatcherAssert.assertThat(actualMessage, CoreMatchers.containsString(expectedMessage));
        return this;
    }

    public Then theStatusCodeWas(final int expectedStatusCode) {
        final int actualStatusCode = testData.getResponse().getStatusCode();
        MatcherAssert.assertThat(actualStatusCode, CoreMatchers.is(expectedStatusCode));
        return this;
    }

    public Then theResponseContentTypeWas(final String expectedContentType) {
        return theReponseContainsTheHeader("Content-Type", expectedContentType);
    }

    public Then theReponseContainsTheHeader(final String key, final String value) {
        final Map<String, String> headers = testData.getResponse().getHeaders();
        final Map<String, String> normalizedHeaders = new HashMap<>();
        headers.forEach((k, v) -> normalizedHeaders.put(k.toLowerCase(), v));
        final String normalizedKey = key.toLowerCase();
        MatcherAssert.assertThat(normalizedHeaders.keySet(), CoreMatchers.hasItem(normalizedKey));
        final String actualValue = normalizedHeaders.get(normalizedKey);
        MatcherAssert.assertThat(actualValue, CoreMatchers.is(value));
        return this;
    }

    public Then theResponseBodyWas(final String expectedResponseBody) {
        final String actualResponseBody = testData.getResponse().getBody();
        MatcherAssert.assertThat(actualResponseBody, CoreMatchers.is(expectedResponseBody));
        return this;
    }

    public Then theResponseBodyContains(final String expectedResponseBody) {
        final String actualResponseBody = testData.getResponse().getBody();
        MatcherAssert.assertThat(actualResponseBody, CoreMatchers.containsString(expectedResponseBody));
        return this;
    }

    public Then theJsonResponseEquals(final String expectedJson) {
        final Map<String, Object> normalizedExpected = normalizeJsonToMap(expectedJson);
        final String actualResponseBody = testData.getResponse().getBody();
        final Map<String, Object> normalizedActual = normalizeJsonToMap(actualResponseBody);
        MatcherAssert.assertThat(normalizedActual, CoreMatchers.is(normalizedExpected));
        return this;
    }

    public Then theCheckpointHasBeenVisited(final String checkpoint) {
        MatcherAssert.assertThat(testData.getCheckpoints().checkpointHasBeenVisited(checkpoint), CoreMatchers.is(true));
        return this;
    }

    public Then aWebsocketMessageHasBeenReceivedWithContent(final String content) {
        return theCheckpointHasBeenVisited(content);
    }
}
