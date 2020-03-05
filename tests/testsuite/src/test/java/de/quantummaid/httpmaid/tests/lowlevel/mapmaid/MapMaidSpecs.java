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

package de.quantummaid.httpmaid.tests.lowlevel.mapmaid;

import com.google.gson.Gson;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.lowlevel.mapmaid.usecases.MyUseCase;
import de.quantummaid.mapmaid.mapper.marshalling.Unmarshaller;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.http.headers.ContentType.fromString;
import static de.quantummaid.httpmaid.http.headers.ContentType.json;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.toConfigureMapMaidUsingRecipe;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.*;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;
import static de.quantummaid.mapmaid.builder.recipes.marshallers.urlencoded.UrlEncodedUnmarshaller.urlEncodedUnmarshaller;

public final class MapMaidSpecs {

    @SuppressWarnings("unchecked")
    private static HttpMaid httpMaid() {
        final Unmarshaller urlEncodedUnmarshaller = urlEncodedUnmarshaller();
        return anHttpMaid()
                .post("/", (request, response) -> request.optionalBodyMap().ifPresent(response::setBody))
                .configured(toUnmarshallContentTypeInRequests(fromString("custom"), string -> Map.of("key", "value")))
                .configured(toMarshallContentTypeInResponses(fromString("custom"), map -> "custom_marshalled"))
                .configured(toUnmarshallContentTypeInRequests(ContentType.formUrlEncoded(), string -> {
                    try {
                        return urlEncodedUnmarshaller.unmarshal(string, Map.class);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }))
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
        testEnvironment.given(httpMaid())
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("a=b").withContentType("application/x-www-form-urlencoded").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("custom")
                .theResponseBodyWas("custom_marshalled");
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mapMaidIntegrationCorrectlyUnmarshallsWithoutSpecifiedRequestContentType(final TestEnvironment testEnvironment) {
        final Gson gson = new Gson();
        testEnvironment.given(
                anHttpMaid()
                        .post("/", MyUseCase.class)
                        .configured(toMarshallContentType(json(), string -> gson.fromJson(string, Map.class), gson::toJson))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{\"field1\": \"foo\", \"field2\": \"bar\"}").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("{}");
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mapMaidIntegrationCanHelpWithValidation(final TestEnvironment testEnvironment) {
        final Gson gson = new Gson();
        testEnvironment.given(
                anHttpMaid()
                        .post("/", MyUseCase.class)
                        .configured(toMarshallContentType(json(),
                                string -> gson.fromJson(string, Map.class),
                                gson::toJson))
                        .configured(toConfigureMapMaidUsingRecipe(mapMaidBuilder -> mapMaidBuilder
                                .withExceptionIndicatingValidationError(IllegalArgumentException.class)))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{\"field1\": \"wrong\", \"field2\": \"wrong\"}").withContentType("application/json").isIssued()
                .theStatusCodeWas(500)
                .theJsonResponseEquals("" +
                        "{" +
                        "\"errors\":[" +
                        "{\"path\":\"field1\",\"message\":\"customPrimitive1 is wrong\"}," +
                        "{\"path\":\"field2\",\"message\":\"customPrimitive2 is wrong\"}" +
                        "]}");
    }
}
