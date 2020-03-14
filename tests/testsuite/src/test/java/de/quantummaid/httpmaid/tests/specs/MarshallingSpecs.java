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

package de.quantummaid.httpmaid.tests.specs;

import de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators;
import de.quantummaid.httpmaid.marshalling.MarshallingException;
import de.quantummaid.httpmaid.marshalling.UnsupportedContentTypeException;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.mapmaid.builder.AdvancedBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static de.quantummaid.httpmaid.http.headers.ContentType.fromString;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.*;

public final class MarshallingSpecs {

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void unmarshallerCanBeSet(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> request.optionalBodyMap().ifPresent(map -> {
                            final Object value = map.get("a");
                            response.setBody((String) value);
                        }))
                        .configured(toUnmarshallContentTypeInRequests(fromString("qwer"), body -> Map.of("a", "b")))
                        .configured(toMarshallByDefaultUsingTheContentType(fromString("qwer")))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("qwer").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("b");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void marshallerCanBeSet(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> request.optionalBodyMap().ifPresent(response::setBody))
                        .configured(toUnmarshallContentTypeInRequests(fromString("qwer"), body -> Map.of("a", "b")))
                        .configured(toMarshallContentTypeInResponses(fromString("qwer"), map -> (String) ((Map) map).get("a")))
                        .configured(toMarshallByDefaultUsingTheContentType(fromString("qwer")))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("qwer").withTheHeader("Accept", "qwer").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("b");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void requestUsesContentTypeHeaderForUnmarshalling(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> request.optionalBodyMap().ifPresent(map -> {
                            final Object value = map.get("a");
                            response.setBody((String) value);
                        }))
                        .configured(toUnmarshallContentTypeInRequests(fromString("wrong"), body -> Map.of("a", "wrong")))
                        .configured(toUnmarshallContentTypeInRequests(fromString("right"), body -> Map.of("a", "right")))
                        .configured(toMarshallByDefaultUsingTheContentType(fromString("wrong")))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("right").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("right");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void defaultContentTypeIsUsedForUnmarshallingIfNoContentTypeIsSpecified(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> request.optionalBodyMap().ifPresent(map -> {
                            final Object value = map.get("a");
                            response.setBody((String) value);
                        }))
                        .configured(toUnmarshallContentTypeInRequests(fromString("wrong"), body -> Map.of("a", "wrong")))
                        .configured(toUnmarshallContentTypeInRequests(fromString("right"), body -> Map.of("a", "right")))
                        .configured(toMarshallByDefaultUsingTheContentType(fromString("right")))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("right");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void responseUsesContentTypeOfAcceptHeader(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> request.optionalBodyMap().ifPresent(response::setBody))
                        .configured(toUnmarshallContentTypeInRequests(fromString("qwer"), body -> Map.of("a", "b")))
                        .configured(toMarshallContentTypeInResponses(fromString("wrong"), map -> "the wrong marshaller"))
                        .configured(toMarshallContentTypeInResponses(fromString("right"), map -> "the right marshaller"))
                        .configured(toMarshallByDefaultUsingTheContentType(fromString("qwer")))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("qwer").withTheHeader("Accept", "right")
                .isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("right")
                .theResponseBodyWas("the right marshaller");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void responseIsMarshalledUsingContentTypeIfNoAcceptHeaderIsSet(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> request.optionalBodyMap().ifPresent(response::setBody))
                        .configured(toUnmarshallContentTypeInRequests(fromString("right"), body -> Map.of("a", "b")))
                        .configured(toMarshallContentTypeInResponses(fromString("wrong"), map -> "the wrong marshaller"))
                        .configured(toMarshallContentTypeInResponses(fromString("right"), map -> "the right marshaller"))
                        .configured(toMarshallByDefaultUsingTheContentType(fromString("right")))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("right")
                .isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("right")
                .theResponseBodyWas("the right marshaller");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void wildcardsInAcceptHeaderCanBeUsedToSpecifyResponseContentType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> request.optionalBodyMap().ifPresent(response::setBody))
                        .configured(toUnmarshallContentTypeInRequests(fromString("wrong/x"), body -> Map.of("a", "b")))
                        .configured(toMarshallContentTypeInResponses(fromString("wrong/x"), map -> "the wrong marshaller"))
                        .configured(toMarshallContentTypeInResponses(fromString("right/x"), map -> "the right marshaller"))
                        .configured(toMarshallByDefaultUsingTheContentType(fromString("wrong/x")))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withTheHeader("Accept", "right/*")
                .isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("right/x")
                .theResponseBodyWas("the right marshaller");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void responseIsMarshalledUsingContentTypeIfAcceptHeaderAllowsMultipleMarshallers(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> request.optionalBodyMap().ifPresent(response::setBody))
                        .configured(toUnmarshallContentTypeInRequests(fromString("right/x"), body -> Map.of("a", "b")))
                        .configured(toUnmarshallContentTypeInRequests(fromString("right/y"), body -> Map.of("a", "c")))
                        .configured(toMarshallContentTypeInResponses(fromString("right/x"), map -> "the right marshaller"))
                        .configured(toMarshallContentTypeInResponses(fromString("right/y"), map -> "the wrong marshaller"))
                        .configured(toMarshallByDefaultUsingTheContentType(fromString("right/y")))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("right/x").withTheHeader("Accept", "right/*")
                .isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("right/x")
                .theResponseBodyWas("the right marshaller");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void responseIsMarshalledUsingDefaultContentTypeIfAcceptAndContentTypeHeaderCannotBeUsed(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> request.optionalBodyMap().ifPresent(response::setBody))
                        .configured(toUnmarshallContentTypeInRequests(fromString("qwer"), body -> Map.of("a", "b")))
                        .configured(toUnmarshallContentTypeInRequests(fromString("asdf"), body -> Map.of("a", "c")))
                        .configured(toMarshallContentTypeInResponses(fromString("qwer"), map -> "right"))
                        .configured(toMarshallByDefaultUsingTheContentType(fromString("qwer")))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("asdf").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("right");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void unknownUnmarshallerCanThrowException(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> request.optionalBodyMap().ifPresent(response::setBody))
                        .configured(toUnmarshallContentTypeInRequests(fromString("qwer"), body -> Map.of("a", "b")))
                        .configured(toMarshallByDefaultUsingTheContentType(fromString("qwer")))
                        .configured(toThrowAnExceptionIfNoMarshallerWasFound())
                        .configured(MapMaidConfigurators.toConfigureMapMaidUsingRecipe(mapMaidBuilder ->
                                mapMaidBuilder.withAdvancedSettings(AdvancedBuilder::doNotAutoloadMarshallers)))
                        .configured(toMapExceptionsOfType(UnsupportedContentTypeException.class, (exception, response) -> {
                            response.setStatus(501);
                            response.setBody(exception.getMessage());
                        }))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("asdf").isIssued()
                .theStatusCodeWas(501)
                .theResponseBodyWas("Content type 'asdf' is not supported; supported content types are: 'qwer'");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void unknownMarshallerCanThrowException(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", (request, response) -> request.optionalBodyMap().ifPresent(response::setBody))
                        .configured(toUnmarshallContentTypeInRequests(fromString("qwer"), body -> Map.of("a", "b")))
                        .configured(toMarshallByDefaultUsingTheContentType(fromString("qwer")))
                        .configured(toThrowAnExceptionIfNoMarshallerWasFound())
                        .configured(toMapExceptionsOfType(MarshallingException.class, (exception, response) -> response.setStatus(501)))
                        .configured(MapMaidConfigurators.toConfigureMapMaidUsingRecipe(mapMaidBuilder ->
                                mapMaidBuilder.withAdvancedSettings(AdvancedBuilder::doNotAutoloadMarshallers)))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().withContentType("qwer").isIssued()
                .theStatusCodeWas(501);
    }
}
