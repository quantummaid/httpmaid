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

package de.quantummaid.httpmaid.tests.givenwhenthen.deploy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiBaseUrlProtocol {
    HTTP("http", "http"),
    HTTPS("https", "https"),
    WS("ws", "http"),
    WSS("wss", "https");

    private final String urlProtocol;
    private final String transportProtocol;

    public static ApiBaseUrlProtocol protocol(final String value) {
        if ("http".equals(value)) {
            return HTTP;
        } else if ("https".equals(value)) {
            return HTTPS;
        } else if ("ws".equals(value)) {
            return WS;
        } else if ("wss".equals(value)) {
            return WSS;
        } else {
            throw new UnsupportedOperationException("Unknown protocol: " + value);
        }
    }

    public String transportProtocol() {
        return transportProtocol;
    }

    public String urlProtocol() {
        return this.urlProtocol;
    }
}
