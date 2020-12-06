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
import de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.mapmaid.minimaljson.MinimalJsonMarshallerAndUnmarshaller.minimalJsonMarshallerAndUnmarshaller;

@SuppressWarnings("java:S1192")
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

                .get("/jsonResponse", (request, response) -> {
                    response.setStatus(201);
                    response.setBody(Map.of("foo", "bar"));
                })
                .get("/statusCode/201", (request, response) -> response.setStatus(201))

                .post("/echo", (request, response) -> response.setBody(request.bodyString()))

                .get("/returnHeader/<name>", (request, response) -> {
                    final String headerName = request.pathParameters().getPathParameter("name");
                    final List<String> header = request.headers().allValuesFor(headerName);
                    response.setBody(Map.of("headers", header));
                })

                .get("/dumpQueryParameters", (request, response) -> {
                    final Map<String, List<String>> params = request.queryParameters().asMap();
                    response.setBody(params);
                })

                .get("/headers/HeaderName/HeaderValue", (request, response) -> response.addHeader("HeaderName", "HeaderValue"))
                .get("/multiValueHeaders/HeaderName/HeaderValue1,HeaderValue2", (request, response) -> {
                    response.addHeader("HeaderName", "HeaderValue1");
                    response.addHeader("HeaderName", "HeaderValue2");
                })
                .get("/cookie", (request, response) -> {
                    final String cookie1 = request.cookies().getCookie("cookie1");
                    final String cookie2 = request.cookies().getCookie("cookie2");
                    response.setBody(cookie1 + " and " + cookie2);
                })
                .get("/setcookies", (request, response) -> {
                    response.setCookie("name", "value");
                    response.setCookie("name2", "value2");
                })
                .get("/setCookiesWithCommas", (request, response) -> {
                    response.setCookie("cookie1", "cookie,value,1");
                    response.setCookie("cookie2", "cookie,value,2");
                })

                .websocket("handler1", (request, response) -> response.setBody("handler 1"))
                .websocket("handler2", (request, response) -> response.setBody("handler 2"))
                .websocket("headerhandler", (request, response) -> {
                    final String header = request.headers().header("X-My-Header");
                    response.setBody(header);
                })

                .post("/broadcast", (request, response) -> request.websockets().sender().sendToAll("foo"))
                .websocket("check", (request, response) -> response.setBody("websocket has been registered"))
                .websocket("disconnect", (request, response) -> request.websockets().disconnector().disconnectAll())
                .configured(MapMaidConfigurators.toConfigureMapMaidUsingRecipe(mapMaidBuilder -> mapMaidBuilder
                        .withAdvancedSettings(advancedBuilder -> advancedBuilder
                                .usingMarshaller(minimalJsonMarshallerAndUnmarshaller()))));
        configurator.accept(builder);
        return builder.build();
    }
}
