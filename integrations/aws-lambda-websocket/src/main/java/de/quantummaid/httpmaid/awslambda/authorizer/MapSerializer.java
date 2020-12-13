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

package de.quantummaid.httpmaid.awslambda.authorizer;

import de.quantummaid.mapmaid.mapper.marshalling.Marshaller;

import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.mapmaid.minimaljson.MinimalJsonMarshaller.minimalJsonMarshaller;

public final class MapSerializer {
    private static final Marshaller<String> MARSHALLER = minimalJsonMarshaller();

    private MapSerializer() {
    }

    public static String toString(final Map<String, Object> map) {
        final Map<String, Object> clonedMap = new HashMap<>(map);
        clonedMap.remove("requestContext");
        try {
            return MARSHALLER.marshal(clonedMap);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
