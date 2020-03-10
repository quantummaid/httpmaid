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

import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.eventmaid.messageBus.MessageBus;

import java.util.Map;

import static de.quantummaid.httpmaid.chains.Configurator.allOf;
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

    public static Configurator toEnrichTheIntermediateMapUsing(final RequestMapEnricher enricher) {
        validateNotNull(enricher, "enricher");
        return configuratorForType(EventModule.class, eventModule -> eventModule.addRequestMapEnricher(enricher));
    }

    public static Configurator toEnrichTheIntermediateMapWithAllHeaders() {
        return toEnrichTheIntermediateMapUsing((map, request) -> {
            final Map<String, String> headersMap = request.headers().asStringMap();
            map.enrichEitherTopOrSecondLevelWithoutOverwriting(headersMap);
        });
    }

    public static Configurator toEnrichTheIntermediateMapWithAllQueryParameters() {
        return toEnrichTheIntermediateMapUsing((map, request) -> {
            final Map<String, String> queryParametersMap = request.queryParameters().asStringMap();
            map.enrichEitherTopOrSecondLevelWithoutOverwriting(queryParametersMap);
        });
    }

    public static Configurator toEnrichTheIntermediateMapWithAllPathParameters() {
        return toEnrichTheIntermediateMapUsing((map, request) -> {
            final Map<String, String> pathParametersMap = request.pathParameters().asStringMap();
            map.enrichEitherTopOrSecondLevelWithoutOverwriting(pathParametersMap);
        });
    }

    public static Configurator toEnrichTheIntermediateMapWithAllRequestData() {
        return allOf(toEnrichTheIntermediateMapWithAllQueryParameters(),
                toEnrichTheIntermediateMapWithAllPathParameters(),
                toEnrichTheIntermediateMapWithAllHeaders());
    }

    public static Configurator toEnrichTheIntermediateMapWithTheAuthenticationInformationAs(final String key) {
        validateNotNullNorEmpty(key, "key");
        return toEnrichTheIntermediateMapUsing((map, request) ->
                request.authenticationInformation()
                        .ifPresent(authenticationInforamtion -> map.enrichOnSecondLevelWithOverwriting(key, authenticationInforamtion)));
    }

    public static Configurator toExtractFromTheResponseMapUsing(final ResponseMapExtractor extractor) {
        validateNotNull(extractor, "extractor");
        return configuratorForType(EventModule.class, eventModule -> eventModule.addResponseMapExtractor(extractor));
    }

    public static Configurator toExtractFromTheResponseMapTheHeader(final String headerKey, final String mapKey) {
        validateNotNullNorEmpty(headerKey, "headerKey");
        validateNotNullNorEmpty(mapKey, "mapKey");
        return toExtractFromTheResponseMapUsing((map, response) -> {
            if(!map.containsKey(mapKey)) {
                return;
            }
            final String value = (String) map.get(mapKey);
            map.remove(mapKey);
            response.addHeader(headerKey, value);
        });
    }
}
