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

import de.quantummaid.httpmaid.chains.*;
import de.quantummaid.httpmaid.generator.builder.ConditionStage;
import de.quantummaid.httpmaid.handler.Handler;
import de.quantummaid.httpmaid.handler.http.HttpHandler;
import de.quantummaid.httpmaid.http.HttpRequestMethod;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMaidBuilder {
    private boolean autodetectionOfModules = true;
    private final CoreModule coreModule;
    private final List<Configurator> configurators;

    static HttpMaidBuilder httpMaidBuilder() {
        return new HttpMaidBuilder(CoreModule.coreModule(), new LinkedList<>());
    }

    public HttpMaidBuilder disableAutodectectionOfModules() {
        autodetectionOfModules = false;
        return this;
    }

    public HttpMaidBuilder get(final String url, final Object handler) {
        return this
                .serving(handler)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.GET);
    }

    public HttpMaidBuilder get(final String url, final HttpHandler handler) {
        return get(url, (Object) handler);
    }

    public HttpMaidBuilder get(final String url, final Processor handler) {
        return get(url, (Handler) handler::apply);
    }

    public HttpMaidBuilder post(final String url, final Object handler) {
        return this
                .serving(handler)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.POST);
    }

    public HttpMaidBuilder post(final String url, final HttpHandler handler) {
        return post(url, (Object) handler);
    }

    public HttpMaidBuilder post(final String url, final Consumer<MetaData> handler) {
        return post(url, (Handler) handler::accept);
    }

    public HttpMaidBuilder put(final String url, final Object handler) {
        return this
                .serving(handler)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.PUT);
    }

    public HttpMaidBuilder put(final String url, final HttpHandler handler) {
        return put(url, (Object) handler);
    }

    public HttpMaidBuilder put(final String url, final Consumer<MetaData> handler) {
        return put(url, (Handler) handler::accept);
    }

    public HttpMaidBuilder delete(final String url, final Object handler) {
        return this
                .serving(handler)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.DELETE);
    }

    public HttpMaidBuilder delete(final String url, final HttpHandler handler) {
        return delete(url, (Object) handler);
    }

    public HttpMaidBuilder delete(final String url, final Consumer<MetaData> handler) {
        return delete(url, (Handler) handler::accept);
    }

    public ConditionStage<HttpMaidBuilder> serving(final Object handler) {
        Validators.validateNotNull(handler, "handler");
        return condition -> {
            coreModule.registerHandler(condition, handler);
            return this;
        };
    }

    public ConditionStage<HttpMaidBuilder> serving(final Handler handler) {
        return serving((Object) handler);
    }

    public HttpMaidBuilder configured(final ConfiguratorBuilder configuratorBuilder) {
        Validators.validateNotNull(configuratorBuilder, "configuratorBuilder");
        final Configurator configurator = configuratorBuilder.build();
        return configured(configurator);
    }

    public HttpMaidBuilder configured(final Configurator configurator) {
        Validators.validateNotNull(configurator, "configurator");
        configurators.add(configurator);
        return this;
    }

    public HttpMaid build() {
        final ChainRegistryBuilder chainRegistryBuilder = ChainRegistryBuilder.chainRegistryBuilder();
        chainRegistryBuilder.addModule(coreModule);
        if (autodetectionOfModules) {
            chainRegistryBuilder.addModuleIfPresent("de.quantummaid.httpmaid.events.EventModule");
            chainRegistryBuilder.addModuleIfPresent("de.quantummaid.httpmaid.usecases.UseCasesModule");
            chainRegistryBuilder.addModuleIfPresent("de.quantummaid.httpmaid.mapmaid.MapMaidModule");
        }
        configurators.forEach(chainRegistryBuilder::addConfigurator);
        final ChainRegistry chainRegistry = chainRegistryBuilder.build();
        return HttpMaid.httpMaid(chainRegistry);
    }
}

