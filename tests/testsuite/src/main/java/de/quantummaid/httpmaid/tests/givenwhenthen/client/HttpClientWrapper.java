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

package de.quantummaid.httpmaid.tests.givenwhenthen.client;

import de.quantummaid.httpmaid.tests.givenwhenthen.builders.MultipartElement;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface HttpClientWrapper extends AutoCloseable {
    HttpClientResponse issueRequestWithoutBody(HttpClientRequest request);

    HttpClientResponse issueRequestWithStringBody(HttpClientRequest request, String body);

    HttpClientResponse issueRequestWithMultipartBody(HttpClientRequest request, List<MultipartElement> parts);

    WrappedWebsocket openWebsocket(Consumer<String> responseHandler,
                                   Runnable closeHandler,
                                   Map<String, List<String>> queryParameters,
                                   Map<String, List<String>> headers);

    @Override
    default void close() {
    }
}
