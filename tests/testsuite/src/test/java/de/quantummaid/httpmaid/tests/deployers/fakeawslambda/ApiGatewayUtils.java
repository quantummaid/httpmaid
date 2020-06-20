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

package de.quantummaid.httpmaid.tests.deployers.fakeawslambda;

import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

import static de.quantummaid.httpmaid.util.streams.Streams.inputStreamToString;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class ApiGatewayUtils {

    private ApiGatewayUtils() {
    }

    public static void addBodyToEvent(final InputStream bodyStream, final Map<String, Object> event) {
        final String body = inputStreamToString(bodyStream);
        if (body.isEmpty()) {
            event.put("body", null);
            event.put("isBase64Encoded", false);
        } else {
            final String encodedBody = encodeBase64(body);
            event.put("body", encodedBody);
            event.put("isBase64Encoded", true);
        }
    }

    private static String encodeBase64(final String unencoded) {
        final Base64.Encoder encoder = Base64.getEncoder();
        final byte[] bytes = encoder.encode(unencoded.getBytes(UTF_8));
        return new String(bytes, UTF_8);
    }
}
