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

package de.quantummaid.httpmaid.documentation.servingfiles;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class StaticFileExampleTests {

    @Test
    public void fileExample() {
        //Showcase start staticFile
        final HttpMaid httpMaid = anHttpMaid()
                .get("/myFile", (request, response) -> response.setFileAsBody("src/test/resources/image.jpg"))
                .build();
        //Showcase end staticFile

        Deployer.test(httpMaid, client -> {
            final SimpleHttpResponseObject response = client.issue(aGetRequestToThePath("/myFile"));
            assertThat(response.getStatusCode(), is(200));
        });
    }

    @Test
    public void resourceExample() {
        //Showcase start javaResource
        final HttpMaid httpMaid = anHttpMaid()
                .get("/resource", (request, response) -> response.setJavaResourceAsBody("myHtmlResource.html"))
                .build();
        //Showcase end javaResource

        Deployer.test(httpMaid, client -> {
            final SimpleHttpResponseObject response = client.issue(aGetRequestToThePath("/resource"));
            assertThat(response.getStatusCode(), is(200));
            assertThat(response.getBody(), containsString("This is some example content."));
        });
    }
}
