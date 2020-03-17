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

import de.quantummaid.eventmaid.messageBus.MessageBus;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.httpmaid.PerRouteConfigurator;
import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.httpmaid.events.enriching.Enricher;

import static de.quantummaid.httpmaid.chains.Configurator.configuratorForType;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;

public final class EventConfigurators {

    private EventConfigurators() {
    }

    public static Configurator toUseTheMessageBus(final MessageBus messageBus) {
        validateNotNull(messageBus, "messageBus");
        return configuratorForType(EventModule.class, eventModule -> eventModule.setMessageBus(messageBus));
    }

    public static Configurator toExtractFromTheResponseMapUsing(final ResponseMapExtractor extractor) {
        validateNotNull(extractor, "extractor");
        return configuratorForType(EventModule.class, eventModule -> eventModule.addResponseMapExtractor(extractor));
    }

    public static Configurator toExtractFromTheResponseMapTheHeader(final String headerKey, final String mapKey) {
        validateNotNullNorEmpty(headerKey, "headerKey");
        validateNotNullNorEmpty(mapKey, "mapKey");
        return toExtractFromTheResponseMapUsing((map, response) -> {
            if (!map.containsKey(mapKey)) {
                return;
            }
            final String value = (String) map.get(mapKey);
            map.remove(mapKey);
            response.addHeader(headerKey, value);
        });
    }

    public static PerRouteConfigurator mappingPathParameter(final String name) {
        return mappingPathParameter(name, name);
    }

    public static PerRouteConfigurator mappingPathParameter(final String parameterName, final String mapKey) {
        return mapping((request, map) -> {
            final String pathParameter = request.pathParameters().getPathParameter(parameterName);
            map.enrichEitherTopOrSecondLevel(mapKey, pathParameter);
        });
    }

    public static PerRouteConfigurator mappingQueryParameter(final String name) {
        return mappingQueryParameter(name, name);
    }

    public static PerRouteConfigurator mappingQueryParameter(final String parameterName, final String mapKey) {
        return mapping((request, map) -> {
            final String queryParameter = request.queryParameters().getQueryParameter(parameterName);
            map.enrichEitherTopOrSecondLevel(mapKey, queryParameter);
        });
    }

    public static PerRouteConfigurator mappingHeader(final String name) {
        return mappingHeader(name, name);
    }

    public static PerRouteConfigurator mappingHeader(final String headerName, final String mapKey) {
        return mapping((request, map) -> {
            final String header = request.headers().getHeader(headerName);
            map.enrichEitherTopOrSecondLevel(mapKey, header);
        });
    }

    public static PerRouteConfigurator mappingAuthenticationInformation(final String key) {
        return mapping((request, map) -> {
            final Object authenticationInformation = request.authenticationInformation();
            map.enrichEitherTopOrSecondLevel(key, authenticationInformation);
        });
    }

    private static PerRouteConfigurator mapping(final Enricher enricher) {
        return (generationCondition, handler, dependencyRegistry) -> {
            if (!(handler instanceof EventType)) {
                return;
            }
            final EventModule eventModule = dependencyRegistry.getDependency(EventModule.class);
            eventModule.addEnricher((EventType) handler, enricher);
        };
    }
}
