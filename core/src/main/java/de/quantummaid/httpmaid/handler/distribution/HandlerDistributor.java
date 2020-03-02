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

import de.quantummaid.httpmaid.generator.GenerationCondition;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HandlerDistributor {
    private final Predicate<Object> predicate;
    private final BiConsumer<Object, GenerationCondition> handlerConsumer;

    public static HandlerDistributor handlerDistributor(final Predicate<Object> predicate,
                                                        final BiConsumer<Object, GenerationCondition> handlerConsumer) {
        Validators.validateNotNull(predicate, "predicate");
        Validators.validateNotNull(handlerConsumer, "handlerConsumer");
        return new HandlerDistributor(predicate, handlerConsumer);
    }

    public boolean appliesTo(final Object handler) {
        return predicate.test(handler);
    }

    public void consume(final Object handler, final GenerationCondition condition) {
        handlerConsumer.accept(handler, condition);
    }
}
