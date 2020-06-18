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

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEventKeys.BODY;
import static java.nio.charset.StandardCharsets.UTF_8;

final class EventUtils {

    private EventUtils() {
    }

    static String extractPotentiallyEncodedBody(final AwsLambdaEvent event) {
        final String rawBody = event.getAsString(BODY);
        if (rawBody == null) {
            return "";
        }
        final Boolean isBase64Encoded = event.getAsBoolean("isBase64Encoded");
        if (isBase64Encoded) {
            return decodeBase64(rawBody);
        } else {
            return rawBody;
        }
    }

    private static String decodeBase64(final String encoded) {
        final Base64.Decoder decoder = Base64.getDecoder();
        final byte[] decoded = decoder.decode(encoded);
        return new String(decoded, UTF_8);
    }
}
