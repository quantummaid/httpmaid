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

package de.quantummaid.httpmaid.usecases.method;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static de.quantummaid.eventmaid.internal.reflections.ForbiddenUseCaseMethods.NOT_ALLOWED_USECASE_PUBLIC_METHODS;
import static de.quantummaid.eventmaid.useCases.useCaseAdapter.methodInvoking.MethodInvocationException.methodInvocationException;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UseCaseMethod {
    private final Class<?> useCaseClass;
    private final Method method;
    private final Parameters parameters;
    private final Class<?> returnType;

    public static UseCaseMethod useCaseMethodOf(final Class<?> useCase) {
        final Method method = locateUseCaseMethod(useCase);
        final Parameters parameters = Parameters.parametersOf(method);
        final Class<?> returnType;
        if (method.getReturnType() == Void.TYPE) {
            returnType = null;
        } else {
            returnType = method.getReturnType();
        }
        return new UseCaseMethod(useCase, method, parameters, returnType);
    }

    public Class<?> useCaseClass() {
        return useCaseClass;
    }

    public boolean isSingleParameterUseCase() {
        return parameters.asMap().size() == 1;
    }

    public String singleParameterName() {
        return parameterNames().get(0);
    }

    public List<String> parameterNames() {
        return parameters.names();
    }

    public Map<String, Class<?>> parameters() {
        return parameters.asMap();
    }

    public Optional<Class<?>> returnType() {
        return ofNullable(returnType);
    }

    public String describe() {
        return method.getName();
    }

    public Optional<Object> invoke(final Object useCase,
                                   final Map<String, Object> parameters,
                                   final Object event) throws Exception {
        final Object[] parameterInstances = this.parameters.toArray(parameters);
        try {
            final Object returnValue = this.method.invoke(useCase, parameterInstances);
            return ofNullable(returnValue);
        } catch (final IllegalAccessException e) {
            final Class<?> useCaseClass = useCase.getClass();
            throw methodInvocationException(useCaseClass, useCase, this.method, event, e);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (isDeclaredByMethod(cause, this.method)) {
                throw (Exception) cause;
            } else {
                final Class<?> useCaseClass = useCase.getClass();
                throw methodInvocationException(useCaseClass, useCase, this.method, event, e);
            }
        }
    }

    private boolean isDeclaredByMethod(final Throwable cause, final Method method) {
        final Class<?>[] exceptionTypes = method.getExceptionTypes();
        final Class<? extends Throwable> exceptionClass = cause.getClass();
        return Arrays.asList(exceptionTypes).contains(exceptionClass);
    }

    private static Method locateUseCaseMethod(final Class<?> useCaseClass) {
        final List<Method> useCaseMethods = getAllPublicMethods(useCaseClass, NOT_ALLOWED_USECASE_PUBLIC_METHODS);
        if (useCaseMethods.size() == 1) {
            return useCaseMethods.get(0);
        } else {
            final String message = String.format("Use case classes must have exactly one public instance method. Found the methods %s " +
                            "for class %s",
                    useCaseMethods, useCaseClass);
            throw new IllegalArgumentException(message);
        }
    }

    private static List<Method> getAllPublicMethods(final Class<?> useCaseClass, final Collection<String> excludedMethods) {
        final Method[] methods = useCaseClass.getMethods();
        return stream(methods)
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> !Modifier.isAbstract(method.getModifiers()))
                .filter(method -> method.getDeclaringClass().equals(useCaseClass))
                .filter(method -> !excludedMethods.contains(method.getName()))
                .collect(toList());
    }
}
