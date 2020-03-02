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

package de.quantummaid.httpmaid.mapmaid;

import de.quantummaid.httpmaid.chains.*;
import de.quantummaid.mapmaid.MapMaid;
import de.quantummaid.mapmaid.builder.recipes.Recipe;

import java.util.LinkedList;
import java.util.List;

import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.mapmaid.MapMaidModule.mapMaidModule;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Collections.singletonList;

public final class MapMaidConfigurators {
    public static final MetaDataKey<List<Recipe>> RECIPES = metaDataKey("RECIPES");

    private MapMaidConfigurators() {
    }

    public static MapMaidIntegrationBuilder toUseMapMaid(final MapMaid mapMaid) {
        validateNotNull(mapMaid, "mapMaid");
        return MapMaidIntegrationBuilder.mapMaidIntegration(mapMaid);
    }

    public static Configurator toConfigureMapMaidUsingRecipe(final Recipe recipe) {
        validateNotNull(recipe, "recipe");
        return new Configurator() {
            @Override
            public List<ChainModule> supplyModulesIfNotAlreadyPreset() {
                return singletonList(mapMaidModule());
            }

            @Override
            public void init(final MetaData configurationMetaData) {
                configurationMetaData.getOrSetDefault(RECIPES, LinkedList::new).add(recipe);
            }

            @Override
            public void configure(final DependencyRegistry dependencyRegistry) {
            }
        };
    }
}
