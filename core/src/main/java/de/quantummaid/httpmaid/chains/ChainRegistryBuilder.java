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

package de.quantummaid.httpmaid.chains;

import de.quantummaid.reflectmaid.ReflectMaid;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static de.quantummaid.httpmaid.chains.DependencyRegistry.load;
import static de.quantummaid.httpmaid.chains.MetaData.emptyMetaData;
import static de.quantummaid.httpmaid.chains.autoloading.Autoloader.loadModule;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChainRegistryBuilder {
    final MetaData metaData = emptyMetaData();
    private final List<ChainModule> modules;
    private final List<Configurator> configurators;

    public static ChainRegistryBuilder chainRegistryBuilder() {
        return new ChainRegistryBuilder(new LinkedList<>(), new LinkedList<>());
    }

    public <T> void addMetaDatum(final MetaDataKey<T> key, final T value) {
        metaData.set(key, value);
    }

    public void addModule(final ChainModule module) {
        validateNotNull(module, "module");
        modules.add(module);
    }

    public void addModuleIfPresent(final ReflectMaid reflectMaid, final String fullyQualifiedClassName) {
        validateNotNullNorEmpty(fullyQualifiedClassName, "fullyQualifiedClassName");
        loadModule(reflectMaid, fullyQualifiedClassName)
                .ifPresent(this::addModule);
    }

    public void addConfigurator(final Configurator configurator) {
        validateNotNull(configurator, "configurator");
        configurators.add(configurator);
    }

    public ChainRegistry build() {
        final DependencyRegistry dependencyRegistry = load(modules, metaData);
        enterDefaultDependencies(modules, dependencyRegistry);
        enterDefaultDependencies(configurators, dependencyRegistry);

        dependencyRegistry.modules().stream().forEach(module -> module.init(metaData));
        configurators.forEach(configurator -> configurator.init(metaData));

        configurators.forEach(configurator -> configurator.configure(dependencyRegistry));
        dependencyRegistry.modules().stream().forEach(module -> module.configure(dependencyRegistry));
        return dependencyRegistry.buildChainRegistry();
    }

    private static void enterDefaultDependencies(final List<? extends Configurator> configurators,
                                                 final DependencyRegistry dependencyRegistry) {
        configurators.stream()
                .map(Configurator::supplyModulesIfNotAlreadyPresent)
                .flatMap(Collection::stream)
                .forEach(module -> enterDefaultDependency(module, dependencyRegistry));
    }

    private static void enterDefaultDependency(final ChainModule dependency,
                                               final DependencyRegistry dependencyRegistry) {
        final List<ChainModule> followUpDependencies =
                dependencyRegistry.addIfNotAlreadyPresentAndReturnFollowUpDependencies(dependency);
        followUpDependencies.forEach(module -> enterDefaultDependency(module, dependencyRegistry));
    }
}
