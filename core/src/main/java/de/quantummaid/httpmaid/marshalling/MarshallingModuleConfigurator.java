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

package de.quantummaid.httpmaid.marshalling;

import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.httpmaid.chains.DependencyRegistry;

import java.util.List;

import static de.quantummaid.httpmaid.marshalling.MarshallingModule.emptyMarshallingModule;
import static java.util.Collections.singletonList;

public interface MarshallingModuleConfigurator extends Configurator {

    @Override
    default List<ChainModule> supplyModulesIfNotAlreadyPresent() {
        return singletonList(emptyMarshallingModule());
    }

    void configure(MarshallingModule marshallingModule);

    @Override
    default void configure(final DependencyRegistry dependencyRegistry) {
        final MarshallingModule marshallingModule = dependencyRegistry.getDependency(MarshallingModule.class);
        configure(marshallingModule);
    }
}
