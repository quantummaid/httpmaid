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

package de.quantummaid.httpmaid.remotespecsinstance;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.HttpMaidBuilder;

import java.util.function.Consumer;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;

public final class HttpMaidFactory {

    private HttpMaidFactory() {
    }

    public static HttpMaid httpMaid() {
        return httpMaid(httpMaidBuilder -> {
        });
    }

    public static HttpMaid httpMaid(final Consumer<HttpMaidBuilder> configurator) {
        final HttpMaidBuilder builder = anHttpMaid()
                .get("/", (request, response) -> response.setBody("fooooo"))
                .get("/statusCode/201", (request, response) -> response.setStatus(201))
                .get("/headers/HeaderName/HeaderValue", (request, response) -> response.addHeader("HeaderName", "HeaderValue"))
                .get("/multiValueHeaders/HeaderName/HeaderValue1,HeaderValue2", (request, response) -> {
                    response.addHeader("HeaderName", "HeaderValue1");
                    response.addHeader("HeaderName", "HeaderValue2");
                })
                .websocket("handler1", (request, response) -> response.setBody("handler 1"))
                .websocket("handler2", (request, response) -> response.setBody("handler 2"))
                .post("/broadcast", (request, response) -> request.websockets().sendToAll("foo"))
                .websocket("check", (request, response) -> response.setBody("websocket has been registered"));
        configurator.accept(builder);
        return builder.build();
    }
}
