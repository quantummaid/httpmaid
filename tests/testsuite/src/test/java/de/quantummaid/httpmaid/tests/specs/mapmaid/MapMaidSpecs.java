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

package de.quantummaid.httpmaid.tests.specs.mapmaid;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.specs.mapmaid.usecases.MyFailingWithEmptyMessageUseCase;
import de.quantummaid.httpmaid.tests.specs.mapmaid.usecases.MyUseCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.events.EventConfigurators.mappingHeader;
import static de.quantummaid.httpmaid.events.EventConfigurators.mappingPathParameter;
import static de.quantummaid.httpmaid.http.headers.ContentType.fromString;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.*;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toMarshallContentTypeInResponses;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toUnmarshallContentTypeInRequests;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;

public final class MapMaidSpecs {

    private static HttpMaid httpMaid() {
        return anHttpMaid()
                .post("/", (request, response) -> request.optionalBodyMap().ifPresent(response::setBody))
                .configured(toUnmarshallContentTypeInRequests(fromString("custom"), string -> Map.of("key", "value")))
                .configured(toMarshallContentTypeInResponses(fromString("custom"), map -> "custom_marshalled"))
                .build();
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mapMaidIntegrationCanUnmarshalCustomFormat(final TestEnvironment testEnvironment) {
        testEnvironment.given(httpMaid())
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("x").withContentType("custom").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("custom")
                .theResponseBodyWas("custom_marshalled");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mapMaidIntegrationCanUnmarshalFormEncodedButDoesNotMarshalFormEncodedByDefault(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> {
                            final Map<String, Object> bodyMap = request.bodyMap();
                            response.setBody(bodyMap);
                        })
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("a=b").withContentType("application/x-www-form-urlencoded").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("{\"a\":\"b\"}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mapMaidIntegrationCorrectlyUnmarshallsWithoutSpecifiedRequestContentType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", MyUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{\"field1\": \"foo\", \"field2\": \"bar\"}").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("{}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mapMaidIntegrationCanHelpWithValidation(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", MyUseCase.class)
                        .configured(toConfigureMapMaidUsingRecipe(mapMaidBuilder -> mapMaidBuilder
                                .withExceptionIndicatingValidationError(IllegalArgumentException.class)))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{\"field1\": \"wrong\", \"field2\": \"wrong\"}").withContentType("application/json").isIssued()
                .theStatusCodeWas(400)
                .theJsonResponseEquals("" +
                        "{" +
                        "\"errors\": [" +
                        "{" +
                        "   \"path\": \"myRequest.field1\"," +
                        "   \"message\": \"customPrimitive1 is wrong\"" +
                        "}," +
                        "{" +
                        "   \"path\": \"myRequest.field2\"," +
                        "   \"message\": \"customPrimitive2 is wrong\"" +
                        "}]}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void automatedValidationResponseWorksWhenExceptionMessageIsNull(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", MyFailingWithEmptyMessageUseCase.class)
                        .configured(toConfigureMapMaidUsingRecipe(mapMaidBuilder -> mapMaidBuilder
                                .withExceptionIndicatingValidationError(IllegalArgumentException.class)))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("\"foo\"").withContentType("application/json").isIssued()
                .theStatusCodeWas(400)
                .theJsonResponseEquals("" +
                        "{" +
                        "\"errors\": [" +
                        "{" +
                        "   \"path\": \"parameter\"" +
                        "}]}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void statusCodeForAutomatedValidationResponseCanBeConfigured(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", MyUseCase.class)
                        .configured(toConfigureMapMaidUsingRecipe(mapMaidBuilder -> mapMaidBuilder
                                .withExceptionIndicatingValidationError(IllegalArgumentException.class)))
                        .configured(toSetStatusCodeOnMapMaidValidationErrorsTo(401))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{\"field1\": \"wrong\", \"field2\": \"wrong\"}").withContentType("application/json").isIssued()
                .theStatusCodeWas(401)
                .theJsonResponseEquals("" +
                        "{" +
                        "\"errors\": [" +
                        "{" +
                        "   \"message\": \"customPrimitive1 is wrong\"," +
                        "   \"path\": \"myRequest.field1\"" +
                        "}," +
                        "{" +
                        "   \"message\": \"customPrimitive2 is wrong\"," +
                        "   \"path\": \"myRequest.field2\"" +
                        "}]}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void automatedValidationResponseCanBeDisabled(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", MyUseCase.class)
                        .configured(toConfigureMapMaidUsingRecipe(mapMaidBuilder -> mapMaidBuilder
                                .withExceptionIndicatingValidationError(IllegalArgumentException.class)))
                        .configured(toNotCreateAnAutomaticResponseForMapMaidValidationErrors())
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{\"field1\": \"wrong\", \"field2\": \"wrong\"}").withContentType("application/json").isIssued()
                .theStatusCodeWas(500)
                .theResponseBodyWas("");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testMapMaidOnlyWithInjectionAndWithoutBody(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/mapmaid/<value1>", MapMaidUseCase.class,
                                mappingPathParameter("value1", "dataTransferObject.value1"),
                                mappingHeader("value2", "dataTransferObject.value2"),
                                mappingHeader("value3", "dataTransferObject.value3"),
                                mappingHeader("value4", "dataTransferObject.value4"))
                        .build()
        )
                .when().aRequestToThePath("/mapmaid/derp").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("value2", "merp").withTheHeader("value3", "herp").withTheHeader("value4", "qerp").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theJsonResponseEquals("" +
                        "{" +
                        "   value1: derp," +
                        "   value2: merp," +
                        "   value3: herp," +
                        "   value4: qerp" +
                        "}"
                );
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void mapMaidCanUnmarshallEmptyStringToFormEncoded(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> response.setBody(request.bodyMap()))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("")
                .withContentType("application/x-www-form-urlencoded").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("{}");
    }
}
