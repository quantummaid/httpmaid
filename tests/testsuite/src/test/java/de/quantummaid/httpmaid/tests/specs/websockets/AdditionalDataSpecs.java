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
import de.quantummaid.httpmaid.tests.specs.websockets.domain.TestUseCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.events.EventConfigurators.mappingAdditionalWebsocketData;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ENVIRONMENTS_WITH_ALL_CAPABILITIES_WITHOUT_SHITTY_CLIENT;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.WEBSOCKET_ENVIRONMENTS;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toStoreAdditionalDataInWebsocketContext;
import static de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria.websocketCriteria;

public final class AdditionalDataSpecs {

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void additionalDataCanBeStoredInWebsocketRegistry(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket((request, response) -> {
                            final Map<String, Object> additionalData = request.additionalData();
                            final String foo = (String) additionalData.get("foo");
                            response.setBody(foo);
                        })
                        .configured(toStoreAdditionalDataInWebsocketContext(request -> Map.of("foo", "bar")))
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("_")
                .oneWebsocketHasReceivedTheMessage("bar");
    }

    @ParameterizedTest
    @MethodSource(ENVIRONMENTS_WITH_ALL_CAPABILITIES_WITHOUT_SHITTY_CLIENT)
    public void additionalDataInWebsocketRegistryCanBeUsedToSelectWebsockets(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/broadcast", (request, response) -> request
                                .websockets().sender().sendTo("foo",
                                websocketCriteria().addititionalDataString("clientId", "a")))
                        .configured(toStoreAdditionalDataInWebsocketContext(request -> {
                            final String id = request.queryParameters().parameter("id");
                            return Map.of("clientId", id);
                        }))
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of("id", List.of("a")), Map.of())
                .andWhen().aWebsocketIsConnected(Map.of("id", List.of("b")), Map.of())
                .andWhen().aRequestToThePath("/broadcast").viaTheGetMethod().withAnEmptyBody().isIssued()
                .oneWebsocketHasReceivedTheMessage("foo");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void additionalDataCanBeUsedInHandler(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket((request, response) -> {
                            final Map<String, Object> additionalData = request.additionalData();
                            final String foo = (String) additionalData.get("foo");
                            response.setBody(foo);
                        })
                        .configured(toStoreAdditionalDataInWebsocketContext(request -> Map.of("foo", "bar")))
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("_")
                .oneWebsocketHasReceivedTheMessage("bar");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void additionalDataCanBeMappedToUseCase(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket(TestUseCase.class, mappingAdditionalWebsocketData("foo", "parameter1"))
                        .configured(toStoreAdditionalDataInWebsocketContext(request -> Map.of("foo", "bar")))
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("" +
                "{ " +
                "   \"parameter2\": \"asdf\"" +
                "}")
                .oneWebsocketHasReceivedTheMessage("\"barasdf\"");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void additionalDataCanBeMappedToUseCaseWithDotNotation(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket(TestUseCase.class, mappingAdditionalWebsocketData("foo[3].d", "parameter1"))
                        .configured(toStoreAdditionalDataInWebsocketContext(request -> Map.of(
                                "foo", List.of(
                                        "a", "b", "c", Map.of(
                                                "d", "e"
                                        )
                                )
                                )
                        ))
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("" +
                "{ " +
                "   \"parameter2\": \"asdf\"" +
                "}")
                .oneWebsocketHasReceivedTheMessage("\"easdf\"");
    }
}
