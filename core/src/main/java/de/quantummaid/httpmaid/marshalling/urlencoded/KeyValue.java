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

package de.quantummaid.httpmaid.marshalling.urlencoded;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.marshalling.urlencoded.Key.parseKey;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.lang.String.format;
import static java.net.URLDecoder.decode;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class KeyValue {
    private final Key key;
    private final String value;

    static KeyValue keyValue(final Key key, final String value) {
        validateNotNull(key, "key");
        validateNotNull(value, "value");
        return new KeyValue(key, value);
    }

    static KeyValue parse(final String keyAndValue) {
        final String[] split = keyAndValue.split("=");
        final String key = decode(split[0], UTF_8);
        final String value;
        if(split.length > 1) {
            value = decode(split[1], UTF_8);
        } else {
            value = "";
        }
        return keyValue(parseKey(key), value);
    }

    Key key() {
        return this.key;
    }

    String value() {
        return this.value;
    }

    String render() {
        final String encodedKey = this.key.render();
        final String encodedValue = encode(this.value, UTF_8);
        return format("%s=%s", encodedKey, encodedValue);
    }
}
