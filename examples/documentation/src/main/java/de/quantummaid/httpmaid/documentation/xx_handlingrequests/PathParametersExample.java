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

package de.quantummaid.httpmaid.documentation.xx_handlingrequests;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.http.PathParameters;
import de.quantummaid.httpmaid.path.Path;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public final class PathParametersExample {

    public static void main(final String[] args) {
        //Showcase start pathParameters2
        final HttpMaid httpMaid = anHttpMaid()
                .get("/hello/<name>", (request, response) -> {
                    final PathParameters pathParameters = request.pathParameters();
                    final String name = pathParameters.getPathParameter("name");
                    response.setBody("hi " + name + "!");
                })
                .build();
        //Showcase end pathParameters2
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
