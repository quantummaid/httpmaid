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

package de.quantummaid.httpmaid.testjar;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.remotespecsinstance.HttpMaidFactory;

import static de.quantummaid.httpmaid.jetty.JettyWebsocketEndpoint.jettyWebsocketEndpoint;

@SuppressWarnings("java:S106")
public final class TestMain {
    private static final HttpMaid HTTP_MAID = HttpMaidFactory.httpMaid();

    private TestMain() {
    }

    public static void main(final String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java -jar <jar> <port>");
        }
        final int port = Integer.parseInt(args[0]);
        jettyWebsocketEndpoint(HTTP_MAID, port);
        System.out.println("\nTestJar ready to be tested.");
    }
}
