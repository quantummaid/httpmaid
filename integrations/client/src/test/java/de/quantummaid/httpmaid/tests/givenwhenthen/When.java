/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

import de.quantummaid.httpmaid.tests.givenwhenthen.domain.ACustomPrimitive;
import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.mapmaid.MapMaid;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Consumer;

import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;
import static de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientForTheHost;
import static de.quantummaid.httpmaid.tests.givenwhenthen.Then.then;
import static de.quantummaid.httpmaid.util.Streams.inputStreamToString;
import static de.quantummaid.mapmaid.MapMaid.aMapMaid;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class When {
    private final int port;
    private final RequestLog requestLog;

    static When when(final int port,
                     final RequestLog requestLog) {
        return new When(port, requestLog);
    }

    public Then aRequestIsMadeToThePath(final String path) {
        return aRequestIsMade(httpMaidClient -> httpMaidClient.issue(aGetRequestToThePath(path)));
    }

    public Then aRequestIsMade(final HttpClientRequestBuilder<?> requestBuilder) {
        return aRequestIsMade(httpMaidClient -> httpMaidClient.issue(requestBuilder));
    }

    public Then aRequestIsMade(final Consumer<HttpMaidClient> clientConsumer) {
        final MapMaid mapMaid = aMapMaid(ACustomPrimitive.class.getPackageName()).build();
        final HttpMaidClient client = aHttpMaidClientForTheHost("localhost")
                .withThePort(port)
                .viaHttp()
                .withDefaultResponseMapping((response, targetType) -> {
                    final String stringContent = inputStreamToString(response.content());
                    return mapMaid.deserializeJson(stringContent, targetType);
                })
                .build();
        clientConsumer.accept(client);
        return then(requestLog);
    }
}
