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

package de.quantummaid.httpmaid.usecases.instantiation;

import de.quantummaid.reflectmaid.resolvedtype.ClassType;
import de.quantummaid.reflectmaid.resolvedtype.ResolvedType;
import de.quantummaid.reflectmaid.resolvedtype.resolver.ResolvedConstructor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Collectors;

import static de.quantummaid.httpmaid.usecases.instantiation.ZeroArgumentsConstructorUseCaseInstantiatorException.zeroArgumentsConstructorUseCaseInstantiatorException;
import static de.quantummaid.reflectmaid.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("java:S1452")
public final class InstantiationInformation {
    private final Constructor<?> constructor;

    public static InstantiationInformation instantiationInformationFor(final ResolvedType type) {
        validateNotInterface(type);
        validateNotAbstractClass(type);
        final ClassType classType = (ClassType) type;
        final Constructor<?> constructor = extractZeroArgumentsConstructor(classType);
        validateNotNull(constructor, "constructor");
        return new InstantiationInformation(constructor);
    }

    public Constructor<?> constructor() {
        return constructor;
    }

    private static Constructor<?> extractZeroArgumentsConstructor(final ClassType type) {
        final List<ResolvedConstructor> relevantConstructors = type.constructors().stream()
                .filter(ResolvedConstructor::isPublic)
                .filter(resolvedConstructor -> resolvedConstructor.getParameters().isEmpty())
                .collect(Collectors.toList());
        if (relevantConstructors.isEmpty()) {
            throw zeroArgumentsConstructorUseCaseInstantiatorException(type, "no zero-argument constructors found");
        }
        return relevantConstructors.get(0).getConstructor();
    }

    private static void validateNotInterface(final ResolvedType type) {
        if (type.isInterface()) {
            throw zeroArgumentsConstructorUseCaseInstantiatorException(type, "must not be an interface");
        }
    }

    private static void validateNotAbstractClass(final ResolvedType type) {
        if (type.isAbstract()) {
            throw zeroArgumentsConstructorUseCaseInstantiatorException(type, "must not be an abstract class");
        }
    }
}
