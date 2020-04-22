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

import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Then {
    private final RequestLog requestLog;
    private final SimpleHttpResponseObject responseObject;

    public static Then then(final RequestLog requestLog,
                            final SimpleHttpResponseObject responseObject) {
        return new Then(requestLog, responseObject);
    }

    public Then theServerReceivedARequestToThePath(final String expectedPath) {
        final Request request = requestLog.lastRequest();
        assertThat(request.path(), is(expectedPath));
        return this;
    }

    public Then theResponseBodyWas(final String body) {
        assertThat(responseObject.getBody(), is(body));
        return this;
    }

    public Then theResponseDescriptionContains(final String description) {
        final String actualDescription = responseObject.describe();
        assertThat(actualDescription, containsString(description));
        return this;
    }
}
