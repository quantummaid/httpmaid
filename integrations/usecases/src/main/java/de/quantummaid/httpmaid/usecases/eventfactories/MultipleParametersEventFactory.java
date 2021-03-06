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

package de.quantummaid.httpmaid.usecases.eventfactories;

import de.quantummaid.httpmaid.events.EventFactory;
import de.quantummaid.httpmaid.events.enriching.EnrichableMap;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.events.enriching.EnrichableMap.enrichableMap;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipleParametersEventFactory implements EventFactory {
    private final List<String> parameterNames;

    public static EventFactory multipleParametersEventFactory(final List<String> parameterNames) {
        validateNotNull(parameterNames, "parameterNames");
        return new MultipleParametersEventFactory(parameterNames);
    }

    @SuppressWarnings("unchecked")
    @Override
    public EnrichableMap createEvent(final Object unmarshalledBody) {
        final EnrichableMap event = enrichableMap(parameterNames);
        if (unmarshalledBody instanceof Map) {
            event.overwriteTopLevel((Map<String, ?>) unmarshalledBody);
        }
        return event;
    }
}
