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

package de.quantummaid.httpmaid.tests.lowlevel.mapmaid;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.lowlevel.mapmaid.usecases.MyUseCase;
import de.quantummaid.httpmaid.tests.lowlevel.mapmaid.usecases.domain.MyRequest;
import com.google.gson.Gson;
import de.quantummaid.mapmaid.MapMaid;
import de.quantummaid.mapmaid.mapper.marshalling.Unmarshaller;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.http.headers.ContentType.fromString;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurator.toUseMapMaid;
import static de.quantummaid.httpmaid.mapmaid.MapMaidIntegration.toMarshalRequestAndResponseBodiesUsingMapMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;
import static de.quantummaid.mapmaid.MapMaid.aMapMaid;
import static de.quantummaid.mapmaid.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe.urlEncodedMarshaller;
import static de.quantummaid.mapmaid.mapper.marshalling.MarshallingType.marshallingType;

public final class MapMaidSpecs {

    private static HttpMaid httpMaid() {
        final MapMaid mapMaid = aMapMaid()
                .usingMarshaller(marshallingType("custom"), o -> "custom_marshalled", new Unmarshaller() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> T unmarshal(final String input, final Class<T> type) {
                        return (T) Map.of("key", "value");
                    }
                })
                .usingRecipe(urlEncodedMarshaller())
                .build();
        return anHttpMaid()
                .post("/", (request, response) -> request.optionalBodyMap().ifPresent(response::setBody))
                .configured(toMarshalRequestAndResponseBodiesUsingMapMaid(mapMaid)
                        .matchingTheContentType(fromString("custom")).toTheMarshallerType(marshallingType("custom")))
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

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mapMaidIntegrationCorrectlyUnmarshallsWithoutSpecifiedRequestContentType(final TestEnvironment testEnvironment) {
        final Gson gson = new Gson();
        final MapMaid mapMaid = aMapMaid(MyRequest.class.getPackageName())
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .build();
        testEnvironment.given(
                anHttpMaid()
                        .post("/", MyUseCase.class)
                        .configured(toUseMapMaid(mapMaid))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{\"field1\": \"foo\", \"field2\": \"bar\"}").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("{}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void mapMaidIntegrationCanHelpWithValidation(final TestEnvironment testEnvironment) {
        final Gson gson = new Gson();
        final MapMaid mapMaid = aMapMaid(MyRequest.class.getPackageName())
                .withExceptionIndicatingValidationError(IllegalArgumentException.class)
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .build();
        testEnvironment.given(
                anHttpMaid()
                        .post("/", MyUseCase.class)
                        .configured(toUseMapMaid(mapMaid))
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
