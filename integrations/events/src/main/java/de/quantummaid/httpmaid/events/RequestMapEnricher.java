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

package de.quantummaid.httpmaid.events;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.handler.http.HttpRequest;

import java.util.Map;

import static de.quantummaid.httpmaid.events.EventModule.EVENT;
import static de.quantummaid.httpmaid.handler.http.HttpRequest.httpRequest;

public interface RequestMapEnricher extends Processor {

    @SuppressWarnings("unchecked")
    @Override
    default void apply(final MetaData metaData) {
        final Object event = metaData.get(EVENT);
        if (event instanceof Map) {
            final HttpRequest httpRequest = httpRequest(metaData);
            enrich((Map<String, Object>) event, httpRequest);
        }
    }

    void enrich(Map<String, Object> map, HttpRequest request);
}
