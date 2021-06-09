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

package de.quantummaid.httpmaid.tests.specs.usecase.specialusecases;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.*;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiator;
import de.quantummaid.reflectmaid.GenericType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.chains.Configurator.toUseModules;
import static de.quantummaid.httpmaid.events.EventModule.eventModule;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsByDefaultUsing;
import static de.quantummaid.httpmaid.mapmaid.MapMaidModule.mapMaidModule;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;
import static de.quantummaid.httpmaid.usecases.UseCaseConfigurators.toCreateUseCaseInstancesUsing;
import static de.quantummaid.httpmaid.usecases.UseCasesModule.useCasesModule;
import static de.quantummaid.reflectmaid.GenericType.genericType;

public final class SpecialUseCaseSpecs {

    /*
    final data class UseCaseResponse<T>(val type: String, val payload: T, val mapMaidFix: String = "");
     */

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithTwoMethods(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithTwoMethods.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("Use case classes must have exactly one public instance (non-static) method.");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithoutPublicMethods(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithoutPublicMethods.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("Use case classes must have exactly one public instance (non-static) method.");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithAdditionalPackagePrivateMethods(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", UseCaseWithAdditionalPackagePrivateMethods.class)
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"method2\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void packagePrivateUseCaseWithPublicMethod(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", PackagePrivateUseCaseWithPublicMethod.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case class must be public but got 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.PackagePrivateUseCaseWithPublicMethod'");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void packagePrivateUseCaseWithPackagePrivateMethod(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", PackagePrivateUseCaseWithPackagePrivateMethod.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case class must be public but got 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.PackagePrivateUseCaseWithPackagePrivateMethod'");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithClassScopeTypeVariableAsDirectReturnType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithClassScopeTypeVariableAsDirectReturnType.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("" +
                        "type 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithClassScopeTypeVariableAsDirectReturnType' " +
                        "contains the following type variables that need to be filled in in order to create a GenericType object: [T]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithClassScopeTypeVariableAsDirectReturnTypeWithGenericType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", genericType(UseCaseWithClassScopeTypeVariableAsDirectReturnType.class, String.class))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(500)
                .theResponseBodyWas("");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithClassScopeTypeVariableAsIndirectReturnType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithClassScopeTypeVariableAsIndirectReturnType.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("" +
                        "type 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithClassScopeTypeVariableAsIndirectReturnType' " +
                        "contains the following type variables that need to be filled in in order to create a GenericType object: [T]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithClassScopeTypeVariableAsIndirectReturnTypeWithGenericType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", genericType(UseCaseWithClassScopeTypeVariableAsIndirectReturnType.class, String.class))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(500)
                .theResponseBodyWas("");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithClassScopeTypeVariableAsDirectParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .post("/", UseCaseWithClassScopeTypeVariableAsDirectParameter.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("type 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithClassScopeTypeVariableAsDirectParameter' " +
                        "contains the following type variables that need to be filled in in order to create a GenericType object: [T]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithClassScopeTypeVariableAsDirectParameterRegisteredAsGenericType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .post("/", genericType(UseCaseWithClassScopeTypeVariableAsDirectParameter.class, String.class))
                        .configured(toMapExceptionsByDefaultUsing((exception, request, response) -> response.setBody(exception.getMessage())))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("\"foo\"").withContentType("application/json").isIssued()
                .theResponseBodyWas("type 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithClassScopeTypeVariableAsDirectParameter' contains the following type variables that need to be filled in in order to create a GenericType object: [T]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithClassScopeTypeVariableAsIndirectParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .post("/", UseCaseWithClassScopeTypeVariableAsIndirectParameter.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("" +
                        "type 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithClassScopeTypeVariableAsIndirectParameter' " +
                        "contains the following type variables that need to be filled in in order to create a GenericType object: [T]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithClassScopeTypeVariableAsIndirectParameterWithGenericType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .post("/", genericType(UseCaseWithClassScopeTypeVariableAsIndirectParameter.class, String.class))
                        .configured(toMapExceptionsByDefaultUsing((exception, request, response) -> response.setBody(exception.getMessage())))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("[\"a\", \"b\", \"c\"]").withContentType("application/json").isIssued()
                .theResponseBodyWas("type 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithClassScopeTypeVariableAsIndirectParameter' contains the following type variables that need to be filled in in order to create a GenericType object: [T]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithMethodScopeTypeVariableAsDirectReturnType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithMethodScopeTypeVariableAsDirectReturnType.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("Use case classes must have exactly one public instance (non-static) method. " +
                        "Found the methods [] for class 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithMethodScopeTypeVariableAsDirectReturnType'. " +
                        "(Note that methods that declare new type variables (\"generics\") are not taken into account)");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithMethodScopeTypeVariableAsIndirectReturnType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithMethodScopeTypeVariableAsIndirectReturnType.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("" +
                        "Use case classes must have exactly one public instance (non-static) method. " +
                        "Found the methods [] for class 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithMethodScopeTypeVariableAsIndirectReturnType'. " +
                        "(Note that methods that declare new type variables (\"generics\") are not taken into account)");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithMethodScopeTypeVariableAsDirectParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithMethodScopeTypeVariableAsDirectParameter.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("Use case classes must have exactly one public instance (non-static) method. " +
                        "Found the methods [] for class 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithMethodScopeTypeVariableAsDirectParameter'. " +
                        "(Note that methods that declare new type variables (\"generics\") are not taken into account)");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithMethodScopeTypeVariableAsIndirectParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithMethodScopeTypeVariableAsIndirectParameter.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("" +
                        "Use case classes must have exactly one public instance (non-static) method. " +
                        "Found the methods [] for class 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithMethodScopeTypeVariableAsIndirectParameter'. " +
                        "(Note that methods that declare new type variables (\"generics\") are not taken into account)");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithWildcardInReturnType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithWildcardInReturnType.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("[type '?' is not supported because it contains wildcard generics (\"?\")]")
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("java.util.List<?> -> because return type of method 'List<?> method()' [public java.util.List<? super java.lang.String> " +
                        "de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithWildcardInReturnType.method()]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithGenericsInReturnType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithGenericsInReturnType.class)
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("[\"a\",\"b\",\"c\"]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithGenericsInParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .post("/", UseCaseWithGenericsInParameter.class)
                        .configured(toMapExceptionsByDefaultUsing((exception, request, response) -> response.setBody(exception.getMessage())))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("[\"a\",\"b\",\"c\"]").withContentType("application/json").isIssued()
                .theResponseBodyWas("{a, b, c}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithWildcardInParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithWildcardInParameter.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("" +
                        "The following classes could not be detected properly:\n" +
                        "\n" +
                        "?: unable to detect deserialization-only:\n" +
                        "no deserialization-only detected:\n" +
                        "[type '?' is not supported because it contains wildcard generics (\"?\")]\n" +
                        "\n" +
                        "?:\n" +
                        "Mode: deserialization-only\n" +
                        "How it is deserialized:\n" +
                        "\tNo deserializer available\n" +
                        "Why it needs to be deserializable:\n" +
                        "\t- java.util.List<?> -> because parameter type of method 'void method(List<?> list)' [public void " +
                        "de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithWildcardInParameter.method(java.util.List<? super java.lang.String>)]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseThatIsAnInterface(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", UseCaseThatIsAnInterface.class)
                        .disableAutodectectionOfModules()
                        .configured(toUseModules(eventModule(), useCasesModule(), mapMaidModule()))
                        .configured(toCreateUseCaseInstancesUsing(new UseCaseInstantiator() {

                            @SuppressWarnings("unchecked")
                            @Override
                            public <T> T instantiate(final GenericType<T> type) {
                                return (T) new UseCaseThatIsAnInterface() {};
                            }
                        }))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"method\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseThatIsAnAbstractClass(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", UseCaseThatIsAnAbstractClass.class)
                        .disableAutodectectionOfModules()
                        .configured(toUseModules(eventModule(), useCasesModule(), mapMaidModule()))
                        .configured(toCreateUseCaseInstancesUsing(new UseCaseInstantiator() {

                            @SuppressWarnings("unchecked")
                            @Override
                            public <T> T instantiate(final GenericType<T> type) {
                                return (T) new UseCaseThatIsAnAbstractClass() {};
                            }
                        }))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"method1\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseThatIsAnEnum(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseThatIsAnEnum.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case must not be an enum but got 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseThatIsAnEnum'");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseThatIsAnAnonymousClass(final TestEnvironment testEnvironment) {
        final Class<?> useCaseClass = new UseCaseThatIsAnInterface() {
        }.getClass();
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", useCaseClass)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case must not be an anonymous class but got 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.SpecialUseCaseSpecs$");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseThatIsAnInnerClass(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseThatIsAnInnerClass.NonStaticInnerClass.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case must not be an inner class but got 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseThatIsAnInnerClass$NonStaticInnerClass'");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseThatIsAStaticInnerClass(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseThatIsAnInnerClass.StaticInnerClass.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case must not be an inner class but got 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseThatIsAnInnerClass$StaticInnerClass'");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseThatIsALocalClass(final TestEnvironment testEnvironment) {
        class UseCase {
            public String method() {
                return "method";
            }
        }

        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCase.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case must not be a local class but got " +
                        "'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.SpecialUseCaseSpecs$1UseCase'");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseThatIsAPrimitive(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", int.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case must not be a primitive but got 'int'");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseThatIsAnAnnotation(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseThatIsAnAnnotation.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case must not be an annotation but got 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseThatIsAnAnnotation'");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseThatIsAnArray(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", String[].class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case must not be an array but got 'java.lang.String[]'");
    }
}
