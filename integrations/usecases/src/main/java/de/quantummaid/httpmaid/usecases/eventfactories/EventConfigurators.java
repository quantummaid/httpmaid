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

import de.quantummaid.httpmaid.PerRouteConfigurator;
import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.mappath.MapPath;
import de.quantummaid.httpmaid.usecases.UseCasesModule;
import de.quantummaid.httpmaid.usecases.eventfactories.enriching.PerEventEnrichers;
import de.quantummaid.httpmaid.usecases.eventfactories.enriching.enrichers.*;
import de.quantummaid.httpmaid.usecases.eventfactories.extraction.ResponseMapExtractor;
import de.quantummaid.usecasemaid.RoutingTarget;

import java.util.Optional;
import java.util.function.Consumer;

import static de.quantummaid.httpmaid.chains.Configurator.configuratorForType;
import static de.quantummaid.httpmaid.usecases.eventfactories.enriching.enrichers.AdditionalDataEnricher.additionalDataEnricher;
import static de.quantummaid.httpmaid.usecases.eventfactories.enriching.enrichers.CookieEnricher.cookieEnricher;
import static de.quantummaid.httpmaid.usecases.eventfactories.enriching.enrichers.HeaderEnricher.headerEnricher;
import static de.quantummaid.httpmaid.usecases.eventfactories.enriching.enrichers.OptionalAuthenticationInformationEnricher.optionalAuthenticationInformationEnricher;
import static de.quantummaid.httpmaid.usecases.eventfactories.enriching.enrichers.PathParameterEnricher.pathParameterEnricher;
import static de.quantummaid.httpmaid.usecases.eventfactories.enriching.enrichers.QueryParameterEnricher.queryParameterEnricher;
import static de.quantummaid.httpmaid.usecases.eventfactories.enriching.enrichers.RequiredAuthenticationInformationEnricher.requiredAuthenticationInformationEnricher;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;

@SuppressWarnings("java:S1905")
public final class EventConfigurators {

    private EventConfigurators() {
    }

    public static Configurator toExtractFromTheResponseMapUsing(final ResponseMapExtractor extractor) {
        validateNotNull(extractor, "extractor");
        return configuratorForType(UseCasesModule.class, eventModule -> eventModule.addResponseMapExtractor(extractor));
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

    public static PerRouteConfigurator mappingAdditionalWebsocketData(final String key) {
        return mappingAdditionalWebsocketData(key, key);
    }

    public static PerRouteConfigurator mappingAdditionalWebsocketData(final String key, final String mapKey) {
        final MapPath mapPath = MapPath.parse(key);
        return mappingAdditionalWebsocketData(mapPath, mapKey);
    }

    public static PerRouteConfigurator mappingAdditionalWebsocketData(final MapPath mapPath, final String mapKey) {
        final AdditionalDataEnricher enricher = additionalDataEnricher(mapPath, mapKey);
        return mapping(perEventEnrichers -> perEventEnrichers.addAuthenticationInformationEnricher(enricher));
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
            if (!(handler instanceof RoutingTarget)) {
                return;
            }
            final UseCasesModule useCasesModule = dependencyRegistry.getDependency(UseCasesModule.class);
            useCasesModule.addExtractor((RoutingTarget) handler, perEventExtractors -> perEventExtractors.setStatusCode(statusCode));
        };
    }

    private static PerRouteConfigurator mapping(final Consumer<PerEventEnrichers> enricher) {
        return (generationCondition, handler, dependencyRegistry) -> {
            if (!(handler instanceof RoutingTarget)) {
                return;
            }
            final UseCasesModule useCasesModule = dependencyRegistry.getDependency(UseCasesModule.class);
            useCasesModule.addEnricher((RoutingTarget) handler, enricher);
        };
    }
}
