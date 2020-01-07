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

package de.quantummaid.httpmaid.documentation.xx_ServingFiles;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.path.Path;
import de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public final class StaticFileExample {

    public static void main(final String[] args) {
        //Showcase start staticFile
        final HttpMaid httpMaid = anHttpMaid()
                .get("/myFile", (request, response) -> response.setFileAsBody("./examples/documentation/src/main/java/de/quantummaid/httpmaid/documentation/xx_ServingFiles/files/image.jpg"))
                .build();
        //Showcase end staticFile
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}