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

package de.quantummaid.httpmaid.mapmaid.advancedscanner.deserialization_wrappers;

import de.quantummaid.mapmaid.MapMaid;
import de.quantummaid.mapmaid.builder.recipes.advancedscanner.deserialization_wrappers.MethodParameterDeserializationWrapper;

import java.util.HashMap;
import java.util.Map;

public final class ZeroParametersDeserializationWrapper implements MethodParameterDeserializationWrapper {

    public static MethodParameterDeserializationWrapper zeroParameters() {
        return new de.quantummaid.mapmaid.builder.recipes.advancedscanner.deserialization_wrappers.ZeroParametersDeserializationWrapper();
    }

    @Override
    public Map<String, Object> deserializeParameters(final Object input, final MapMaid mapMaid) {
        return new HashMap<>(0);
    }
}
