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

package de.quantummaid.httpmaid.handler.distribution;

import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.generator.GenerationCondition;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static de.quantummaid.httpmaid.handler.distribution.HandlerDistributor.handlerDistributor;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HandlerDistributors {
    public static final MetaDataKey<HandlerDistributors> HANDLER_DISTRIBUTORS = MetaDataKey.metaDataKey("HANDLER_DISTRIBUTORS");

    private final List<HandlerDistributor> distributors;

    public static HandlerDistributors handlerDistributors() {
        return new HandlerDistributors(new LinkedList<>());
    }

    public void register(final Predicate<Object> predicate,
                         final BiConsumer<Object, GenerationCondition> handlerConsumer) {
        final HandlerDistributor handlerDistributor = handlerDistributor(predicate, handlerConsumer);
        distributors.add(handlerDistributor);
    }

    public void distribute(final Object handler, final GenerationCondition condition) {
        final Optional<HandlerDistributor> match = distributors.stream()
                .filter(handlerDistributor -> handlerDistributor.appliesTo(handler))
                .findFirst();
        if (match.isEmpty()) {
            throw HandlerDistributorException.handlerDistributorException(handler);
        }
        final HandlerDistributor handlerDistributor = match.get();
        handlerDistributor.consume(handler, condition);
    }
}
