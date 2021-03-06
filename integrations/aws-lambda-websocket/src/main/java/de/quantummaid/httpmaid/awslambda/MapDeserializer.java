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

package de.quantummaid.httpmaid.awslambda;

import de.quantummaid.mapmaid.mapper.marshalling.Unmarshaller;

import java.util.Map;

import static de.quantummaid.mapmaid.minimaljson.MinimalJsonUnmarshaller.minimalJsonUnmarshaller;

@SuppressWarnings("java:S4508")
public final class MapDeserializer {
    private static final Unmarshaller<String> UNMARSHALLER = minimalJsonUnmarshaller();

    private MapDeserializer() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> mapFromString(final String string) {
        try {
            return (Map<String, Object>) UNMARSHALLER.unmarshal(string);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
