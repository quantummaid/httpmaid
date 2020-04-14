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

package de.quantummaid.httpmaid.tests.specs.usecase.enriching;

import de.quantummaid.httpmaid.exceptions.ExceptionConfigurators;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.specs.usecase.usecases.SingleStringParameterUseCase;
import de.quantummaid.httpmaid.tests.specs.usecase.usecases.TwoStringsParameterUseCase;
import de.quantummaid.mapmaid.debug.MapMaidException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.events.EventConfigurators.*;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthenticateUsingPathParameter;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthenticateUsingQueryParameter;

public class EnrichingSpecs {

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void pathParameterEnrichmentIsAddedAutomatically(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/<parameter>", SingleStringParameterUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/foo").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void pathParameterEnrichmentIsAddedAutomaticallyForRegexPaths(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/|(?<parameter>.*)|", SingleStringParameterUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/foo").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void pathParameterEnrichmentCanBeIgnored(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/<parameter>", SingleStringParameterUseCase.class, ignorePathParameter("parameter"))
                        .configured(ExceptionConfigurators.toMapExceptionsOfType(MapMaidException.class, (exception, response) -> response.setBody(exception.getMessage())))
                        .build()
        )
                .when().aRequestToThePath("/foo").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyContains("Requiring the input to be an 'string' but found '{}' at 'parameter'");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void pathParameterEnrichmentCanBeConfiguredPerRoute(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/<parameter>", SingleStringParameterUseCase.class, mappingPathParameter("parameter"))
                        .build()
        )
                .when().aRequestToThePath("/foo").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void queryParameterEnrichmentCanBeConfiguredPerRoute(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", SingleStringParameterUseCase.class, mappingQueryParameter("parameter"))
                        .build()
        )
                .when().aRequestToThePath("/?parameter=foo").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void queryParameterFromEnrichmentCanBeEmpty(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", SingleStringParameterUseCase.class, mappingQueryParameter("parameter"))
                        .build()
        )
                .when().aRequestToThePath("/?parameter=").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void twoQueryParametersCanBeEnriched(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", TwoStringsParameterUseCase.class, mappingQueryParameter("parameter1"), mappingQueryParameter("parameter2"))
                        .build()
        )
                .when().aRequestToThePath("/?parameter1=foo&parameter2=bar").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"foobar\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void twoQueryParametersCanEnrichedATopLevelDto(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", DtoUseCase.class,
                                mappingQueryParameter("value1", "dataTransferObject.value1"),
                                mappingQueryParameter("value2", "dataTransferObject.value2")
                        )
                        .build()
        )
                .when().aRequestToThePath("/?value1=foo&value2=bar").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"foobar\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void headerEnrichmentCanBeConfiguredPerRoute(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", SingleStringParameterUseCase.class, mappingHeader("parameter"))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().withTheHeader("parameter", "foo").isIssued()
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void multipleEnrichmentCanBeConfiguredPerRoute(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/<pathParameter>", TwoStringsParameterUseCase.class,
                                mappingPathParameter("pathParameter", "parameter1"),
                                mappingQueryParameter("queryParameter", "parameter2")
                        )
                        .build()
        )
                .when().aRequestToThePath("/foo?queryParameter=bar").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"foobar\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void cookieEnrichmentCanBeConfiguredPerRoute(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", SingleStringParameterUseCase.class, mappingCookie("parameter"))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().withTheHeader("Cookie", "parameter=foo").isIssued()
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void authenticationInformationEnrichmentCanBeConfiguredPerRoute(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/<user>", SingleStringParameterUseCase.class, mappingAuthenticationInformation("parameter"))
                        .configured(toAuthenticateUsingPathParameter("user", Optional::ofNullable))
                        .build()
        )
                .when().aRequestToThePath("/foo").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void whenAuthenticationInformationIsMissingTheRequestIsAborted(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", SingleStringParameterUseCase.class, mappingAuthenticationInformation("parameter"))
                        .configured(toAuthenticateUsingQueryParameter("user", Optional::ofNullable)
                                .notFailingOnMissingAuthentication())
                        .configured(ExceptionConfigurators.toMapExceptionsByDefaultUsing((exception, response) -> response.setBody(exception.getMessage())))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("Request is not authenticated");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void optionalAuthenticationInformationEnrichmentCanBeConfiguredPerRoute(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/<user>", SingleStringParameterUseCase.class, mappingOptionalAuthenticationInformation("parameter"))
                        .configured(toAuthenticateUsingPathParameter("user", Optional::ofNullable))
                        .build()
        )
                .when().aRequestToThePath("/foo").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void whenAuthenticationInformationIsMissingTheRequestIsNotAbortedForOptionalAuthenticationInformation(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", SingleStringParameterUseCase.class, mappingOptionalAuthenticationInformation("parameter"))
                        .configured(toAuthenticateUsingQueryParameter("user", Optional::ofNullable)
                                .notFailingOnMissingAuthentication())
                        .configured(ExceptionConfigurators.toMapExceptionsByDefaultUsing((exception, response) -> response.setBody(exception.getMessage())))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyContains("Requiring the input to be an 'string' but found '{}' at 'parameter'");
    }
}
