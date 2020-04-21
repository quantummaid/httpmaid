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

package de.quantummaid.httpmaid.documentation.quickstart;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import de.quantummaid.httpmaid.endpoint.purejavaendpoint.PureJavaEndpoint;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.endpoint.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public final class QuickstartExampleTests {

    @Test
    public void quickstartExample() {
        //Showcase start quickstart
        final HttpMaid httpMaid = anHttpMaid()
                .get("/hello", (request, response) -> response.setBody("Hi."))
                .build();
        final PureJavaEndpoint endpoint = pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
        //Showcase end quickstart

        Deployer.test(httpMaid, client -> Deployer.assertGet("/hello", "Hi.", client));
    }

    @Test
    public void quickstartPartsExample() {
        //Showcase start quickstartPart1
        final HttpMaid httpMaid = anHttpMaid()
                .get("/hello", (request, response) -> response.setBody("Hi."))
                .build();
        //Showcase end quickstartPart1

        //Showcase start quickstartPart2
        final PureJavaEndpoint endpoint = pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
        //Showcase end quickstartPart2

        Deployer.test(httpMaid, client -> Deployer.assertGet("/hello", "Hi.", client));

        //Showcase start quickstartPart3
        httpMaid.close();
        //Showcase end quickstartPart3
    }
}
