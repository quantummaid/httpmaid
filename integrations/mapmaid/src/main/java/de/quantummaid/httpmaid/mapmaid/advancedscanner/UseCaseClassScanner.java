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

package de.quantummaid.httpmaid.mapmaid.advancedscanner;

import de.quantummaid.httpmaid.mapmaid.advancedscanner.deserialization_wrappers.MethodParameterDeserializationWrapper;
import de.quantummaid.httpmaid.usecases.method.UseCaseMethod;
import de.quantummaid.mapmaid.builder.MapMaidBuilder;
import de.quantummaid.mapmaid.builder.customtypes.DeserializationOnlyType;
import de.quantummaid.mapmaid.mapper.deserialization.deserializers.TypeDeserializer;
import de.quantummaid.reflectmaid.GenericType;
import de.quantummaid.reflectmaid.resolvedtype.ResolvedType;
import de.quantummaid.reflectmaid.typescanner.TypeIdentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.mapmaid.advancedscanner.VirtualDeserializer.virtualDeserializerFor;
import static de.quantummaid.httpmaid.mapmaid.advancedscanner.deserialization_wrappers.MultipleParametersDeserializationWrapper.multipleParameters;
import static de.quantummaid.mapmaid.builder.RequiredCapabilities.deserialization;
import static de.quantummaid.mapmaid.builder.RequiredCapabilities.serialization;
import static de.quantummaid.mapmaid.builder.customtypes.DeserializationOnlyType.deserializationOnlyType;
import static de.quantummaid.mapmaid.shared.validators.NotNullValidator.validateNotNull;
import static de.quantummaid.reflectmaid.typescanner.TypeIdentifier.uniqueVirtualTypeIdentifier;
import static java.lang.String.format;

public final class UseCaseClassScanner {

    private UseCaseClassScanner() {
    }

    public static Map<ResolvedType, MethodParameterDeserializationWrapper> addAllReferencedClassesIn(final List<UseCaseMethod> useCaseMethods,
                                                                                                 final MapMaidBuilder builder) {
        validateNotNull(useCaseMethods, "useCaseMethods");
        validateNotNull(builder, "builder");
        final Map<ResolvedType, MethodParameterDeserializationWrapper> deserializationWrappers = new HashMap<>(useCaseMethods.size());
        useCaseMethods.forEach(useCaseMethod -> {
            final MethodParameterDeserializationWrapper deserializationWrapper = addMethod(useCaseMethod, builder);
            deserializationWrappers.put(useCaseMethod.useCaseClass(), deserializationWrapper);
        });
        return deserializationWrappers;
    }

    private static MethodParameterDeserializationWrapper addMethod(final UseCaseMethod method,
                                                                   final MapMaidBuilder builder) {
        final Map<String, ResolvedType> parameters = method.parameters();
        parameters.values().stream()
                .map(GenericType::fromResolvedType)
                .forEach(type -> builder.withType(
                        type, deserialization(), format("because parameter type of method %s", method.describe())));

        method.returnType().ifPresent(type -> {
            final GenericType<?> genericType = GenericType.fromResolvedType(type);
            builder.withType(
                    genericType,
                    serialization(),
                    format("because return type of method %s", method.describe()));
        });

        final DeserializationOnlyType<?> virtualType = createVirtualObjectFor(method.describe(), parameters);
        builder.deserializing(virtualType);
        return multipleParameters(virtualType.type());
    }

    private static DeserializationOnlyType<?> createVirtualObjectFor(final String method, final Map<String, ResolvedType> parameters) {
        final TypeIdentifier typeIdentifier = uniqueVirtualTypeIdentifier();
        final TypeDeserializer deserializer = virtualDeserializerFor(method, parameters);
        return deserializationOnlyType(typeIdentifier, deserializer);
    }
}
