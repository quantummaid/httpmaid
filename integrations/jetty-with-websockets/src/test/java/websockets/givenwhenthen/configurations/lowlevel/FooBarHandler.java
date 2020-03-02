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

package websockets.givenwhenthen.configurations.lowlevel;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.handler.Handler;
import de.quantummaid.httpmaid.http.Headers;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_HEADERS;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_BODY_STRING;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FooBarHandler implements Handler {

    public static Handler fooBarHandler() {
        return new FooBarHandler();
    }

    @Override
    public void handle(final MetaData metaData) {
        final Headers headers = metaData.get(REQUEST_HEADERS);
        final String foo = headers.getOptionalHeader("foo").orElseThrow();
        metaData.set(RESPONSE_BODY_STRING, foo);
    }
}
