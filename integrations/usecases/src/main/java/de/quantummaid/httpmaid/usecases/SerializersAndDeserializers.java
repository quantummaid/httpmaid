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

package de.quantummaid.httpmaid.usecases;

import de.quantummaid.eventmaid.internal.collections.filtermap.FilterMapBuilder;
import de.quantummaid.eventmaid.internal.collections.predicatemap.PredicateMapBuilder;
import de.quantummaid.eventmaid.mapping.Demapifier;
import de.quantummaid.eventmaid.mapping.Mapifier;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.LowLevelUseCaseAdapterBuilder;
import de.quantummaid.httpmaid.usecases.serializing.SerializerAndDeserializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import static de.quantummaid.eventmaid.internal.collections.filtermap.FilterMapBuilder.filterMapBuilder;
import static de.quantummaid.eventmaid.internal.collections.predicatemap.PredicateMapBuilder.predicateMapBuilder;
import static de.quantummaid.httpmaid.usecases.UseCases.useCases;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class SerializersAndDeserializers {
    private Function<UseCases, SerializerAndDeserializer> serializerAndDeserializer;

    static SerializersAndDeserializers serializersAndDeserializers() {
        return new SerializersAndDeserializers();
    }

    void setSerializerAndDeserializer(final Function<UseCases, SerializerAndDeserializer> serializerAndDeserializer) {
        this.serializerAndDeserializer = serializerAndDeserializer;
    }

    void add(final LowLevelUseCaseAdapterBuilder lowLevelUseCaseAdapterBuilder,
             final Collection<Class<?>> registeredUseCases) {
        final UseCases useCases = useCases(registeredUseCases);
        final SerializerAndDeserializer serializerAndDeserializer = this.serializerAndDeserializer.apply(useCases);
        final PredicateMapBuilder<Object, Mapifier<Object>> serializers = toPredicateMap(serializerAndDeserializer);
        lowLevelUseCaseAdapterBuilder.setReseponseSerializers(serializers);
        final FilterMapBuilder<Class<?>, Object, Demapifier<?>> deserializers = toFilterMap(serializerAndDeserializer);
        lowLevelUseCaseAdapterBuilder.setRequestDeserializers(deserializers);
    }

    private PredicateMapBuilder<Object, Mapifier<Object>> toPredicateMap(final SerializerAndDeserializer serializerAndDeserializer) {
        final PredicateMapBuilder<Object, Mapifier<Object>> responseSerializers = predicateMapBuilder();
        responseSerializers.put(Objects::isNull, object -> null);
        responseSerializers.setDefaultValue(serializerAndDeserializer::serialize);
        return responseSerializers;
    }

    private FilterMapBuilder<Class<?>, Object, Demapifier<?>> toFilterMap(final SerializerAndDeserializer serializerAndDeserializer) {
        final FilterMapBuilder<Class<?>, Object, Demapifier<?>> requestDeserializers = filterMapBuilder();
        requestDeserializers.setDefaultValue(serializerAndDeserializer::deserialize);
        return requestDeserializers;
    }
}
