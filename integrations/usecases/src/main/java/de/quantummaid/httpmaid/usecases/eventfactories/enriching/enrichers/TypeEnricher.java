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

package de.quantummaid.httpmaid.usecases.eventfactories.enriching.enrichers;

import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.usecases.eventfactories.enriching.Event;

import java.util.Optional;

public interface TypeEnricher extends Enricher {

    Optional<Object> extractValue(HttpRequest request);

    @Override
    default void enrich(final HttpRequest httpRequest, final Event event) {
        extractValue(httpRequest)
                .ifPresent(event::addTypeInjection);
    }
}
