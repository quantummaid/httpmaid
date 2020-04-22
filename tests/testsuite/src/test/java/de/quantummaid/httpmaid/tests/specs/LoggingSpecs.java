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

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.logger.LoggerConfigurators.toLogUsing;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;

public final class LoggingSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void traceMessage(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> request.logger().trace("foo"))
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("TRACE: foo");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void traceException(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final IllegalArgumentException exception = new IllegalArgumentException("foo");
                            request.logger().trace(exception);
                        })
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("TRACE: java.lang.IllegalArgumentException: foo\n" +
                        "\tat de.quantummaid.httpmaid");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void traceMessageAndException(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final IllegalArgumentException exception = new IllegalArgumentException("foo");
                            request.logger().trace(exception, "bar");
                        })
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("TRACE: bar\n" +
                        "java.lang.IllegalArgumentException: foo\n" +
                        "\tat de.quantummaid.httpmaid");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void debugMessage(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> request.logger().debug("foo"))
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("DEBUG: foo");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void debugException(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final IllegalArgumentException exception = new IllegalArgumentException("foo");
                            request.logger().debug(exception);
                        })
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("DEBUG: java.lang.IllegalArgumentException: foo\n" +
                        "\tat de.quantummaid.httpmaid");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void debugMessageAndException(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final IllegalArgumentException exception = new IllegalArgumentException("foo");
                            request.logger().debug(exception, "bar");
                        })
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("DEBUG: bar\n" +
                        "java.lang.IllegalArgumentException: foo\n" +
                        "\tat de.quantummaid.httpmaid");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void infoMessage(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> request.logger().info("foo"))
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("INFO: foo");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void infoException(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final IllegalArgumentException exception = new IllegalArgumentException("foo");
                            request.logger().info(exception);
                        })
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("INFO: java.lang.IllegalArgumentException: foo\n" +
                        "\tat de.quantummaid.httpmaid");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void infoMessageAndException(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final IllegalArgumentException exception = new IllegalArgumentException("foo");
                            request.logger().info(exception, "bar");
                        })
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("INFO: bar\n" +
                        "java.lang.IllegalArgumentException: foo\n" +
                        "\tat de.quantummaid.httpmaid");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void warnMessage(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> request.logger().warn("foo"))
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("WARN: foo");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void warnException(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final IllegalArgumentException exception = new IllegalArgumentException("foo");
                            request.logger().warn(exception);
                        })
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("WARN: java.lang.IllegalArgumentException: foo\n" +
                        "\tat de.quantummaid.httpmaid");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void warnMessageAndException(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final IllegalArgumentException exception = new IllegalArgumentException("foo");
                            request.logger().warn(exception, "bar");
                        })
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("WARN: bar\n" +
                        "java.lang.IllegalArgumentException: foo\n" +
                        "\tat de.quantummaid.httpmaid");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void errorMessage(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> request.logger().error("foo"))
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("ERROR: foo");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void errorException(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final IllegalArgumentException exception = new IllegalArgumentException("foo");
                            request.logger().error(exception);
                        })
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("ERROR: java.lang.IllegalArgumentException: foo\n" +
                        "\tat de.quantummaid.httpmaid");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void errorMessageAndException(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final IllegalArgumentException exception = new IllegalArgumentException("foo");
                            request.logger().error(exception, "bar");
                        })
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("ERROR: bar\n" +
                        "java.lang.IllegalArgumentException: foo\n" +
                        "\tat de.quantummaid.httpmaid");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void fatalMessage(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> request.logger().fatal("foo"))
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("FATAL: foo");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void fatalException(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final IllegalArgumentException exception = new IllegalArgumentException("foo");
                            request.logger().fatal(exception);
                        })
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("FATAL: java.lang.IllegalArgumentException: foo\n" +
                        "\tat de.quantummaid.httpmaid");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void fatalMessageAndException(final TestEnvironment testEnvironment) {
        testEnvironment.given(logger ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final IllegalArgumentException exception = new IllegalArgumentException("foo");
                            request.logger().fatal(exception, "bar");
                        })
                        .configured(toLogUsing(logger))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theLogOutputStartedWith("FATAL: bar\n" +
                        "java.lang.IllegalArgumentException: foo\n" +
                        "\tat de.quantummaid.httpmaid");
    }
}
