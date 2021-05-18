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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudformationResource {
    private final CloudformationName name;
    private final String type;
    private final List<CloudformationResource> dependencies;
    private final Map<String, Object> properties;

    public static CloudformationResource cloudformationResource(final CloudformationName name,
                                                                final String type,
                                                                final Map<String, Object> properties) {
        return cloudformationResource(name, type, List.of(), properties);
    }

    public static CloudformationResource cloudformationResource(final CloudformationName name,
                                                                final String type,
                                                                final List<CloudformationResource> dependencies,
                                                                final Map<String, Object> properties) {
        return new CloudformationResource(name, type, dependencies, properties);
    }

    public Object attribute(final String attribute) {
        return IntrinsicFunctions.getAttribute(name, attribute);
    }

    public Object reference() {
        return IntrinsicFunctions.reference(name);
    }

    public CloudformationName name() {
        return name;
    }

    public Map<String, Object> render() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("Type", type);
        if (!dependencies.isEmpty()) {
            final List<String> dependencyNames = dependencies.stream()
                    .map(CloudformationResource::name)
                    .map(CloudformationName::asId)
                    .collect(toList());
            map.put("DependsOn", dependencyNames);
        }
        map.put("Properties", properties);
        return map;
    }
}
