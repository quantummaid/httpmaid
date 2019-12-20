/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid.documentation.xx_handlingrequests;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.http.Headers;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public final class RequestHeadersExample {

    public static void main(final String[] args) {
        final HttpMaid httpMaid = anHttpMaid()
                .get("/hello", (request, response) -> {
                    final Headers headers = request.headers();
                    final String name = headers.getHeader("name");
                    response.setBody("hi " + name + "!");
                })
                .build();
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
