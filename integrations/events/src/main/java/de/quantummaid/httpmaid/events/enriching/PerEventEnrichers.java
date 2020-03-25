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

package de.quantummaid.httpmaid.events.enriching;

import de.quantummaid.httpmaid.events.Event;
import de.quantummaid.httpmaid.events.enriching.enrichers.*;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PerEventEnrichers {
    private final List<PathParameterEnricher> pathParameterEnrichers = new ArrayList<>();
    private final List<PathParameterEnricher> removedPathParameterEnrichers = new ArrayList<>();
    private final List<QueryParameterEnricher> queryParameterEnrichers = new ArrayList<>();
    private final List<HeaderEnricher> headerEnrichers = new ArrayList<>();
    private final List<CookieEnricher> cookieEnrichers = new ArrayList<>();
    private final List<Enricher> authenticationInformationEnrichers = new ArrayList<>();

    public static PerEventEnrichers perEventEnrichers() {
        return new PerEventEnrichers();
    }

    public void addPathParameterEnricher(final PathParameterEnricher enricher) {
        pathParameterEnrichers.add(enricher);
    }

    public void removePathParameterEnricher(final PathParameterEnricher enricher) {
        removedPathParameterEnrichers.add(enricher);
    }

    public void addQueryParameterEnricher(final QueryParameterEnricher enricher) {
        queryParameterEnrichers.add(enricher);
    }

    public void addHeaderEnricher(final HeaderEnricher enricher) {
        headerEnrichers.add(enricher);
    }

    public void addCookieEnricher(final CookieEnricher enricher) {
        cookieEnrichers.add(enricher);
    }

    public void addAuthenticationInformationEnricher(final Enricher enricher) {
        this.authenticationInformationEnrichers.add(enricher);
    }

    public void enrich(final HttpRequest httpRequest, final Event event) {
        pathParameterEnrichers.removeAll(removedPathParameterEnrichers);
        pathParameterEnrichers.forEach(enricher -> enricher.enrich(httpRequest, event));
        queryParameterEnrichers.forEach(enricher -> enricher.enrich(httpRequest, event));
        headerEnrichers.forEach(enricher -> enricher.enrich(httpRequest, event));
        cookieEnrichers.forEach(enricher -> enricher.enrich(httpRequest, event));
        authenticationInformationEnrichers.forEach(enricher -> enricher.enrich(httpRequest, event));
    }
}
