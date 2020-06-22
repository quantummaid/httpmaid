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

package de.quantummaid.httpmaid;

import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.httpmaid.chains.ConfiguratorBuilder;
import de.quantummaid.httpmaid.generator.builder.ConditionStage;
import de.quantummaid.httpmaid.handler.Handler;
import de.quantummaid.httpmaid.handler.http.HttpHandler;
import de.quantummaid.httpmaid.http.HttpRequestMethod;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

public interface HttpConfiguration<T> {

    default T get(final String url, final Object handler, final PerRouteConfigurator... perRouteConfigurators) {
        return this
                .serving(handler, perRouteConfigurators)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.GET);
    }

    default T get(final String url, final HttpHandler handler, final PerRouteConfigurator... perRouteConfigurators) {
        return get(url, (Object) handler, perRouteConfigurators);
    }

    default T post(final String url, final Object handler, final PerRouteConfigurator... perRouteConfigurators) {
        return this
                .serving(handler, perRouteConfigurators)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.POST);
    }

    default T post(final String url, final HttpHandler handler) {
        return post(url, (Object) handler);
    }

    default T put(final String url, final Object handler, final PerRouteConfigurator... perRouteConfigurators) {
        return this
                .serving(handler, perRouteConfigurators)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.PUT);
    }

    default T put(final String url, final HttpHandler handler) {
        return put(url, (Object) handler);
    }

    default T delete(final String url, final Object handler, final PerRouteConfigurator... perRouteConfigurators) {
        return this
                .serving(handler, perRouteConfigurators)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.DELETE);
    }

    default T delete(final String url, final HttpHandler handler) {
        return delete(url, (Object) handler);
    }

    default ConditionStage<T> serving(final Handler handler) {
        return serving((Object) handler);
    }

    ConditionStage<T> serving(Object handler, PerRouteConfigurator... perRouteConfigurators);

    default T configured(final ConfiguratorBuilder configuratorBuilder) {
        validateNotNull(configuratorBuilder, "configuratorBuilder");
        final Configurator configurator = configuratorBuilder.build();
        return configured(configurator);
    }

    T configured(Configurator configurator);
}
