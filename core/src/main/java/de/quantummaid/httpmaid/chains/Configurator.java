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

import java.util.List;
import java.util.function.Consumer;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public interface Configurator {

    static Configurator toUseModules(final ChainModule... modules) {
        validateNotNull(modules, "modules");
        return new Configurator() {
            @Override
            public List<ChainModule> supplyModulesIfNotAlreadyPresent() {
                return asList(modules);
            }

            @Override
            public void configure(final DependencyRegistry dependencyRegistry) {
                // do nothing
            }
        };
    }

    static <T extends ChainModule> Configurator configuratorForType(final Class<T> type, Consumer<T> configurator) {
        validateNotNull(type, "type");
        validateNotNull(configurator, "configurator");
        return dependencyRegistry -> {
            final T module = dependencyRegistry.getDependency(type);
            configurator.accept(module);
        };
    }

    default List<ChainModule> supplyModulesIfNotAlreadyPresent() {
        return emptyList();
    }

    default void init(final MetaData configurationMetaData) {
    }

    void configure(DependencyRegistry dependencyRegistry);
}
