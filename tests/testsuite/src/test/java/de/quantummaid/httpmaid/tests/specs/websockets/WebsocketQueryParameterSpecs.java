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

package de.quantummaid.httpmaid.tests.specs.websockets;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsByDefaultUsing;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.WEBSOCKET_ENVIRONMENTS;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.WEBSOCKET_ENVIRONMENTS_WITHOUT_SHITTY_CLIENT;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toDropAllQueryParametersInWebsocketMessages;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toRememberQueryParametersInWebsocketMessages;

public final class WebsocketQueryParameterSpecs {

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS_WITHOUT_SHITTY_CLIENT)
    public void websocketsCanAccessQueryParametersByDefault(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMaid()
                .websocket("handler", (request, response) -> {
                    final String queryParameter = request.queryParameters().parameter("param+1 %ü");
                    response.setBody(queryParameter);
                })
                .build()
        )
                .when().aWebsocketIsConnected(Map.of("param+1 %ü", List.of("value+1 %ü")), Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .oneWebsocketHasReceivedTheMessage("value+1 %ü");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS_WITHOUT_SHITTY_CLIENT)
    public void allQueryParametersCanBeDroppedForWebsockets(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMaid()
                .websocket("handler", (request, response) -> {
                    final String queryParameter = request.queryParameters().parameter("param+1 %ü");
                    response.setBody(queryParameter);
                })
                .configured(toDropAllQueryParametersInWebsocketMessages())
                .configured(toMapExceptionsByDefaultUsing((exception, request, response) ->
                        response.setBody(exception.getMessage())))
                .build()
        )
                .when().aWebsocketIsConnected(Map.of("param+1 %ü", List.of("value+1 %ü")), Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .oneWebsocketHasReceivedTheMessage("No query parameter with the name 'param+1 %ü'");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS_WITHOUT_SHITTY_CLIENT)
    public void queryParametersCanBeAllowListedForWebsockets(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMaid()
                .websocket("handler1", (request, response) -> {
                    final String queryParameter = request.queryParameters().parameter("allowed");
                    response.setBody(queryParameter);
                })
                .websocket("handler2", (request, response) -> {
                    final String queryParameter = request.queryParameters().parameter("denied");
                    response.setBody(queryParameter);
                })
                .configured(toRememberQueryParametersInWebsocketMessages("allowed"))
                .configured(toMapExceptionsByDefaultUsing((exception, request, response) ->
                        response.setBody(exception.getMessage())))
                .build()
        )
                .when().aWebsocketIsConnected(Map.of("allowed", List.of("value")), Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler1\" }")
                .oneWebsocketHasReceivedTheMessage("value")

                .andWhen().aWebsocketIsConnected(Map.of("denied", List.of("value")), Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler2\" }")
                .oneWebsocketHasReceivedTheMessage("No query parameter with the name 'denied'");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketsAccessMultiValuedQueryStringParameterInExplodedForm(final TestEnvironment testEnvironment) {
        testEnvironment.given(checkpoints ->
                anHttpMaid()
                        .websocket("handler", (request, response) -> {
                            final Map<String, List<String>> parameterMap = request.queryParameters().asMap();
                            response.setBody(parameterMap);
                        })
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of(
                "param1", List.of("value1", "value2"),
                "otherparam", List.of("othervalue"
                )
        ), Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .allWebsocketsHaveReceivedTheJsonMessage(Map.of(
                        "otherparam", List.of("othervalue"),
                        "param1", List.of("value1", "value2")
                ));
    }
}
