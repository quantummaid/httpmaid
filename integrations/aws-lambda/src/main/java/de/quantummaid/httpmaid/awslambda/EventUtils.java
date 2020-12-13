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

import java.util.Base64;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class EventUtils {

    private EventUtils() {
    }

    public static boolean isAuthorizationRequest(final Map<String, Object> event) {
        if (!isWebSocketRequest(event)) {
            return false;
        }
        final String type = (String) event.get("type");
        return "REQUEST".equals(type);
    }

    @SuppressWarnings("unchecked")
    public static boolean isWebSocketRequest(final Map<String, Object> event) {
        final Map<String, Object> context = (Map<String, Object>) event.get("requestContext");
        return context.containsKey("connectionId");
    }

    public static String extractMethodArn(final AwsLambdaEvent event) {
        return event.getAsString("methodArn");
    }

    static String extractPotentiallyEncodedBody(final AwsLambdaEvent event) {
        return event.getAsOptionalString("body")
                .map(rawBody -> {
                    final boolean isBase64Encoded = event.getAsBoolean("isBase64Encoded");
                    if (isBase64Encoded) {
                        return decodeBase64(rawBody);
                    } else {
                        return rawBody;
                    }
                })
                .orElse("");
    }

    private static String decodeBase64(final String encoded) {
        final Base64.Decoder decoder = Base64.getDecoder();
        final byte[] decoded = decoder.decode(encoded);
        return new String(decoded, UTF_8);
    }
}
