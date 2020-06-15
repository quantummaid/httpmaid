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
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.*;
import java.util.stream.Collectors;

import static de.quantummaid.httpmaid.tests.givenwhenthen.JsonNormalizer.normalizeJsonToMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
        assertThat(actualMessage, CoreMatchers.containsString(expectedMessage));
        return this;
    }

    public Then theStatusCodeWas(final int expectedStatusCode) {
        final int actualStatusCode = testData.getResponse().getStatusCode();
        assertThat(actualStatusCode, is(expectedStatusCode));
        return this;
    }

    public Then theResponseContentTypeWas(final String expectedContentType) {
        return theReponseContainsTheHeader("Content-Type", expectedContentType);
    }

    public Then theReponseContainsTheHeader(final String key, final String... values) {
        final Map<String, List<String>> headers = testData.getResponse().getHeaders();
        final Map<String, List<String>> normalizedHeaders = normalizeHeaderNames(headers);
        final String normalizedKey = key.toLowerCase();
        final List<String> potentiallyCommaSeparatedValues = normalizedHeaders.get(normalizedKey);
        final List<String> normalizedValues = normalizeHeaderValues(potentiallyCommaSeparatedValues);
        final List<String> expectedValues = Arrays.asList(values);
        assertThat(normalizedValues, is(expectedValues));
        return this;
    }

    public Then theReponseContainsTheHeaderRawValue(final String key, final String rawValue) {
        final Map<String, List<String>> headers = testData.getResponse().getHeaders();
        final String normalizedKey = key.toLowerCase();
        final String expectedValue = rawValue;
        final List<String> actualValue = headers.get(normalizedKey);
        assertThat("there is one and only one header by that name",  actualValue.size(), is(1));
        assertThat("the header by that name has a value matching exactly our expected value",
                actualValue.get(0), is(expectedValue));
        return this;
    }

    public Then theReponseContainsTheHeaderRawValues(final String key, final String... expectedRawValues) {
        final Map<String, List<String>> headers = testData.getResponse().getHeaders();
        final String normalizedKey = key.toLowerCase();
        final List<String> actualRawValues = headers.get(normalizedKey);
        assertThat(actualRawValues, is(Arrays.asList(expectedRawValues)));
        return this;
    }

    private Map<String, List<String>> normalizeHeaderNames(final Map<String, List<String>> headers) {
        final Map<String, List<String>> normalizedHeaders = new HashMap<>();
        headers.forEach((k, v) -> {
            final String headerName = k.toLowerCase();
            final List<String> headerValues = normalizedHeaders.getOrDefault(k, new ArrayList<>());
            headerValues.addAll(v);
            normalizedHeaders.put(headerName, headerValues);
        });
        return normalizedHeaders;
    }

    private List<String> normalizeHeaderValues(final List<String> values) {
        return values.stream()
                .map(this::normalizeHeaderValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<String> normalizeHeaderValue(final String value) {
        return Arrays.asList(value.split(","));
    }

    public Then theResponseBodyWas(final String expectedResponseBody) {
        final String actualResponseBody = testData.getResponse().getBody();
        assertThat(actualResponseBody, is(expectedResponseBody));
        return this;
    }

    public Then theResponseBodyContains(final String expectedResponseBody) {
        final String actualResponseBody = testData.getResponse().getBody();
        assertThat(actualResponseBody, CoreMatchers.containsString(expectedResponseBody));
        return this;
    }

    public Then theJsonResponseEquals(final String expectedJson) {
        final Map<String, Object> normalizedExpected = normalizeJsonToMap(expectedJson);
        return theJsonResponseStrictlyEquals(normalizedExpected);
    }

    public Then theJsonResponseStrictlyEquals(final Map<String, Object> expectedJsonMap) {
        final String actualResponseBody = testData.getResponse().getBody();
        final Map<String, Object> actualJsonMap = normalizeJsonToMap(actualResponseBody);
        try {
            JSONAssert.assertEquals(new JSONObject(expectedJsonMap), new JSONObject(actualJsonMap), true);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Then theCheckpointHasBeenVisited(final String checkpoint) {
        assertThat(testData.getCheckpoints().checkpointHasBeenVisited(checkpoint), is(true));
        return this;
    }

    public Then aWebsocketMessageHasBeenReceivedWithContent(final String content) {
        return theCheckpointHasBeenVisited(content);
    }
}
