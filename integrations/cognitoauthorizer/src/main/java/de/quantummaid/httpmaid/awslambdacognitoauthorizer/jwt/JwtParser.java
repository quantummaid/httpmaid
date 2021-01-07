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

package de.quantummaid.httpmaid.awslambdacognitoauthorizer.jwt;

import de.quantummaid.mapmaid.mapper.marshalling.Unmarshaller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.jwt.JwtInformation.jwtInformation;
import static de.quantummaid.mapmaid.minimaljson.MinimalJsonUnmarshaller.minimalJsonUnmarshaller;

public final class JwtParser {
    private static final Unmarshaller<String> UNMARSHALLER = minimalJsonUnmarshaller();

    private JwtParser() {
    }

    public static JwtInformation extractJwtPayload(final String token) {
        final String[] parts = token.split("\\.");
        final String encodedPayload = parts[1];

        final Base64.Decoder decoder = Base64.getDecoder();
        final byte[] bytes = decoder.decode(encodedPayload);
        final String payload = new String(bytes, StandardCharsets.UTF_8);
        final Map<String, Object> payloadMap = unmarshal(payload);
        return jwtInformation(payloadMap);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> unmarshal(final String payload) {
        try {
            return (Map<String, Object>) UNMARSHALLER.unmarshal(payload);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
