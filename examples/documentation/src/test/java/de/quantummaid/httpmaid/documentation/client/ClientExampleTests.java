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

package de.quantummaid.httpmaid.documentation.client;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.endpoint.purejavaendpoint.PureJavaEndpoint;
import de.quantummaid.httpmaid.util.Streams;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aPostRequestToThePath;
import static de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientBypassingRequestsDirectlyTo;
import static de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientForTheHost;
import static de.quantummaid.httpmaid.client.body.multipart.Part.aPartWithTheControlName;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ClientExampleTests {

    @Test
    public void clientExample() {
        final HttpMaid httpMaid = anHttpMaid()
                .get("/foo", (request, response) -> response.setBody("foo"))
                .post("/placeOrder", (request, response) -> response.setBody("foo"))
                .post("/upload", (request, response) -> response.setBody("foo"))
                .build();
        final PureJavaEndpoint endpoint = PureJavaEndpoint.pureJavaEndpointFor(httpMaid).listeningOnThePort(8080);

        //Showcase start clientExample
        final HttpMaidClient httpMaidClient = aHttpMaidClientForTheHost("localhost")
                .withThePort(8080)
                .viaHttp()
                .build();
        //Showcase end clientExample

        final InputStream myStream = Streams.stringToInputStream("foo");
        //Showcase start clientUsageExamples
        final SimpleHttpResponseObject response = httpMaidClient.issue(aGetRequestToThePath("/foo"));

        final String stringResponse = httpMaidClient.issue(aGetRequestToThePath("/foo").mappedToString());

        final SimpleHttpResponseObject httpResponseObject = httpMaidClient.issue(
                aPostRequestToThePath("/upload")
                        .withAMultipartBodyWithTheParts(
                                aPartWithTheControlName("file")
                                        .withTheFileName("file.txt")
                                        .withTheContent(myStream)
                        )
        );
        //Showcase end clientUsageExamples

        assertThat(response.getBody(), is("foo"));
        assertThat(stringResponse, is("foo"));
        assertThat(httpResponseObject.getBody(), is("foo"));

        //Showcase start clientToSameHttpMaidInstanceExample
        final HttpMaidClient connectedHttpMaidClient = aHttpMaidClientBypassingRequestsDirectlyTo(httpMaid).build();
        //Showcase end clientToSameHttpMaidInstanceExample

        final SimpleHttpResponseObject directResponse = connectedHttpMaidClient.issue(aGetRequestToThePath("/foo"));
        assertThat(directResponse.getBody(), is("foo"));

        endpoint.close();
    }
}
