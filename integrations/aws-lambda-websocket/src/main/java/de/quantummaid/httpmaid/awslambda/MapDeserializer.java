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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.Map;

@SuppressWarnings("java:S4508")
public final class MapDeserializer {
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private MapDeserializer() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> mapFromString(final String string) {
        return (Map<String, Object>) fromString(string);
    }

    public static Object fromString(final String string) {
        final byte[] data = DECODER.decode(string);
        try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return stream.readObject();
        } catch (final IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
