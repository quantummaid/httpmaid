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

package de.quantummaid.httpmaid.documentation.xx_client;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;

import java.io.InputStream;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aPostRequestToThePath;
import static de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientBypassingRequestsDirectlyTo;
import static de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientForTheHost;
import static de.quantummaid.httpmaid.client.body.multipart.Part.aPartWithTheControlName;

public final class ClientExample {

    public static void main(final String[] args) {
        //Showcase start clientExample
        final HttpMaidClient httpMaidClient = aHttpMaidClientForTheHost("example.org")
                .withThePort(8080)
                .viaHttps()
                .build();
        //Showcase end clientExample

        //Showcase start clientUsageExamples
        final SimpleHttpResponseObject response = httpMaidClient.issue(aGetRequestToThePath("/foo"));

        final String stringResponse = httpMaidClient.issue(aGetRequestToThePath("/foo").mappedToString());

        final OrderConfirmation orderConfirmation = httpMaidClient.issue(
                aPostRequestToThePath("/placeOrder")
                        .withTheBody("{articleId: 4324923}")
                        .mappedTo(OrderConfirmation.class));

        final InputStream myStream = null;
        final SimpleHttpResponseObject httpResponseObject = httpMaidClient.issue(
                aPostRequestToThePath("/upload")
                        .withAMultipartBodyWithTheParts(
                                aPartWithTheControlName("file")
                                        .withTheFileName("file.txt")
                                        .withTheContent(myStream)
                        )
        );
        //Showcase end clientUsageExamples


        final HttpMaid httpMaid = anHttpMaid()
                .build();

        //Showcase start clientToSameHttpMaidInstanceExample
        final HttpMaidClient connectedHttpMaidClient = aHttpMaidClientBypassingRequestsDirectlyTo(httpMaid).build();
        //Showcase end clientToSameHttpMaidInstanceExample
    }
}
