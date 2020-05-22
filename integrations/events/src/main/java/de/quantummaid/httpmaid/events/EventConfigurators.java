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

import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.httpmaid.PerRouteConfigurator;
import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.httpmaid.events.enriching.PerEventEnrichers;
import de.quantummaid.httpmaid.events.enriching.enrichers.*;
import de.quantummaid.httpmaid.handler.http.HttpRequest;

import java.util.Optional;
import java.util.function.Consumer;

import static de.quantummaid.httpmaid.chains.Configurator.configuratorForType;
import static de.quantummaid.httpmaid.events.enriching.enrichers.CookieEnricher.cookieEnricher;
import static de.quantummaid.httpmaid.events.enriching.enrichers.HeaderEnricher.headerEnricher;
import static de.quantummaid.httpmaid.events.enriching.enrichers.OptionalAuthenticationInformationEnricher.optionalAuthenticationInformationEnricher;
import static de.quantummaid.httpmaid.events.enriching.enrichers.PathParameterEnricher.pathParameterEnricher;
import static de.quantummaid.httpmaid.events.enriching.enrichers.QueryParameterEnricher.queryParameterEnricher;
import static de.quantummaid.httpmaid.events.enriching.enrichers.RequiredAuthenticationInformationEnricher.requiredAuthenticationInformationEnricher;
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
        final PathParameterEnricher enricher = pathParameterEnricher(parameterName, mapKey);
        return mapping(perEventEnrichers -> perEventEnrichers.addPathParameterEnricher(enricher));
    }

    public static PerRouteConfigurator ignorePathParameter(final String name) {
        final PathParameterEnricher enricher = pathParameterEnricher(name, name);
        return mapping(perEventEnrichers -> perEventEnrichers.removePathParameterEnricher(enricher));
    }

    public static PerRouteConfigurator mappingQueryParameter(final String name) {
        return mappingQueryParameter(name, name);
    }

    public static PerRouteConfigurator mappingQueryParameter(final String parameterName, final String mapKey) {
        final QueryParameterEnricher enricher = queryParameterEnricher(parameterName, mapKey);
        return mapping(perEventEnrichers -> perEventEnrichers.addQueryParameterEnricher(enricher));
    }

    public static PerRouteConfigurator mappingHeader(final String name) {
        return mappingHeader(name, name);
    }

    public static PerRouteConfigurator mappingHeader(final String headerName, final String mapKey) {
        final HeaderEnricher enricher = headerEnricher(headerName, mapKey);
        return mapping(perEventEnrichers -> perEventEnrichers.addHeaderEnricher(enricher));
    }

    public static PerRouteConfigurator mappingCookie(final String name) {
        return mappingCookie(name, name);
    }

    public static PerRouteConfigurator mappingCookie(final String cookieName, final String mapKey) {
        final CookieEnricher enricher = cookieEnricher(cookieName, mapKey);
        return mapping(perEventEnrichers -> perEventEnrichers.addCookieEnricher(enricher));
    }

    public static PerRouteConfigurator mappingAuthenticationInformation(final String key) {
        final RequiredAuthenticationInformationEnricher enricher = requiredAuthenticationInformationEnricher(key);
        return mapping(perEventEnrichers -> perEventEnrichers.addAuthenticationInformationEnricher(enricher));
    }

    public static PerRouteConfigurator mappingAuthenticationInformation() {
        final TypeEnricher enricher = request -> {
            final Object authenticationInformation = request.authenticationInformation();
            return Optional.of(authenticationInformation);
        };
        return mapping(perEventEnrichers -> perEventEnrichers.addAuthenticationInformationEnricher(enricher));
    }

    public static PerRouteConfigurator mappingOptionalAuthenticationInformation(final String key) {
        final OptionalAuthenticationInformationEnricher enricher = optionalAuthenticationInformationEnricher(key);
        return mapping(perEventEnrichers -> perEventEnrichers.addAuthenticationInformationEnricher(enricher));
    }

    public static PerRouteConfigurator mappingOptionalAuthenticationInformation() {
        final TypeEnricher enricher = HttpRequest::optionalAuthenticationInformation;
        return mapping(perEventEnrichers -> perEventEnrichers.addAuthenticationInformationEnricher(enricher));
    }

    public static PerRouteConfigurator statusCode(final int statusCode) {
        return (generationCondition, handler, dependencyRegistry) -> {
            if (!(handler instanceof EventType)) {
                return;
            }
            final EventModule eventModule = dependencyRegistry.getDependency(EventModule.class);
            eventModule.addExtractor((EventType) handler, perEventExtractors -> perEventExtractors.setStatusCode(statusCode));
        };
    }

    private static PerRouteConfigurator mapping(final Consumer<PerEventEnrichers> enricher) {
        return (generationCondition, handler, dependencyRegistry) -> {
            if (!(handler instanceof EventType)) {
                return;
            }
            final EventModule eventModule = dependencyRegistry.getDependency(EventModule.class);
            eventModule.addEnricher((EventType) handler, enricher);
        };
    }
}
