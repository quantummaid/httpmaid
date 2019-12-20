/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid.chains;

import de.quantummaid.httpmaid.MetricsProvider;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static de.quantummaid.httpmaid.MetricsProvider.metricsProvider;
import static de.quantummaid.httpmaid.chains.IndexedModules.indexedModules;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Collections.emptyList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DependencyRegistry {
    private final IndexedModules modules;
    private final MetaData metaData;

    static DependencyRegistry load(final List<ChainModule> modules,
                                   final MetaData metaData) {
        validateNotNull(modules, "modules");
        validateNotNull(metaData, "metaData");
        return new DependencyRegistry(indexedModules(modules), metaData);
    }

    public <T extends ChainModule> T getDependency(final Class<T> dependency) {
        validateNotNull(dependency, "dependency");
        return modules.get(dependency);
    }

    public <T> void setMetaDatum(final MetaDataKey<T> key, final T value) {
        metaData.set(key, value);
    }

    public <T> T getMetaDatum(final MetaDataKey<T> key) {
        return metaData.get(key);
    }

    public <T> MetricsProvider<T> createMetricsProvider(final MetaDataKey<T> key, final T defaultValue) {
        validateNotNull(key, "key");
        validateNotNull(defaultValue, "defaultValue");
        metaData.set(key, defaultValue);
        return metricsProvider(key, metaData);
    }

    IndexedModules modules() {
        return modules;
    }

    List<ChainModule> addIfNotAlreadyPresentAndReturnFollowUpDependencies(final ChainModule module) {
        validateNotNull(module, "module");
        final Class<? extends ChainModule> key = module.getClass();
        if(modules.contains(key)) {
            return emptyList();
        }
        modules.add(module);
        return module.supplyModulesIfNotAlreadyPreset();
    }

    ChainRegistry buildChainRegistry() {
        final ChainRegistry chainRegistry = ChainRegistry.emptyChainRegistry(metaData);
        modules.stream().forEach(chainRegistry::extend);
        return chainRegistry;
    }
}
