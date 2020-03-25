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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.chains.Configurator.toUseModules;
import static de.quantummaid.httpmaid.events.EventModule.eventModule;
import static de.quantummaid.httpmaid.mapmaid.MapMaidModule.mapMaidModule;
import static de.quantummaid.httpmaid.usecases.UseCaseConfigurators.toCreateUseCaseInstancesUsing;
import static de.quantummaid.httpmaid.usecases.UseCasesModule.useCasesModule;

public final class SpecialUseCaseSpecs {

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseWithTwoMethods(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithTwoMethods.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case classes must have exactly one public instance (non-static) method.");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseWithoutPublicMethods(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithoutPublicMethods.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case classes must have exactly one public instance (non-static) method.");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
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
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
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
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
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
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseWithClassScopeTypeVariableAsDirectReturnType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithClassScopeTypeVariableAsDirectReturnType.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case class 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithClassScopeTypeVariableAsDirectReturnType' must not declare any type variables");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseWithClassScopeTypeVariableAsIndirectReturnType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithClassScopeTypeVariableAsIndirectReturnType.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case class 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithClassScopeTypeVariableAsIndirectReturnType' must not declare any type variables");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseWithClassScopeTypeVariableAsDirectParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithClassScopeTypeVariableAsDirectParameter.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case class 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithClassScopeTypeVariableAsDirectParameter' must not declare any type variables");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseWithClassScopeTypeVariableAsIndirectParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithClassScopeTypeVariableAsIndirectParameter.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case class 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithClassScopeTypeVariableAsIndirectParameter' must not declare any type variables");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseWithMethodScopeTypeVariableAsDirectReturnType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithMethodScopeTypeVariableAsDirectReturnType.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case method 'method' in class 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithMethodScopeTypeVariableAsDirectReturnType' must not declare any type variables");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseWithMethodScopeTypeVariableAsIndirectReturnType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithMethodScopeTypeVariableAsIndirectReturnType.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case method 'method' in class 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithMethodScopeTypeVariableAsIndirectReturnType' must not declare any type variables");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseWithMethodScopeTypeVariableAsDirectParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithMethodScopeTypeVariableAsDirectParameter.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case method 'method' in class 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithMethodScopeTypeVariableAsDirectParameter' must not declare any type variables");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseWithMethodScopeTypeVariableAsIndirectParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithMethodScopeTypeVariableAsIndirectParameter.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case method 'method' in class 'de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseWithMethodScopeTypeVariableAsIndirectParameter' must not declare any type variables");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseWithWildcardInReturnType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithWildcardInReturnType.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("Type variables of 'java.util.List' cannot be resolved");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseWithWildcardInParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseWithWildcardInParameter.class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("Type variables of 'java.util.List' cannot be resolved");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseThatIsAnInterface(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", UseCaseThatIsAnInterface.class)
                        .disableAutodectectionOfModules()
                        .configured(toUseModules(eventModule(), useCasesModule(), mapMaidModule()))
                        .configured(toCreateUseCaseInstancesUsing(new UseCaseInstantiator() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public <T> T instantiate(final Class<T> type) {
                                return (T) new UseCaseThatIsAnInterface() {};
                            }
                        }))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"method\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseThatIsAnAbstractClass(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", UseCaseThatIsAnAbstractClass.class)
                        .disableAutodectectionOfModules()
                        .configured(toUseModules(eventModule(), useCasesModule(), mapMaidModule()))
                        .configured(toCreateUseCaseInstancesUsing(new UseCaseInstantiator() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public <T> T instantiate(final Class<T> type) {
                                return (T) new UseCaseThatIsAnAbstractClass() {};
                            }
                        }))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"method1\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
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
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
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
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
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
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
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
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
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
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
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
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
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
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseThatIsAnArray(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", String[].class)
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("use case must not be an array but got '[Ljava.lang.String;'");
    }
}
