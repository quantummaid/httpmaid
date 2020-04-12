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

package de.quantummaid.httpmaid.documentation.multipart;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import de.quantummaid.httpmaid.multipart.MultipartPart;
import de.quantummaid.httpmaid.multipart.handler.MultipartHandler;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aPostRequestToThePath;
import static de.quantummaid.httpmaid.client.body.multipart.Part.aPartWithTheControlName;
import static de.quantummaid.httpmaid.multipart.MultipartConfigurators.toExposeMultipartBodiesUsingMultipartIteratorBody;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class MultipartExampleTests {

    @Test
    public void multipartExample() {
        //Showcase start multipart
        final HttpMaid httpMaid = anHttpMaid()
                .get("/upload", (request, response) -> response.setJavaResourceAsBody("upload.html"))
                .post("/upload", (request, response) -> System.out.println(request.bodyString()))
                .build();
        //Showcase end multipart

        Deployer.test(httpMaid, client -> {
            final String form = client.issue(aGetRequestToThePath("/upload").mappedToString());
            assertThat(form, containsString("<input type=\"file\" name=\"myFile\" />"));
            Deployer.assertPost("/upload", "foo", "", client);
        });
    }

    @Test
    public void multipartIteratorExample() {
        //Showcase start multipartIterator
        final HttpMaid httpMaid = anHttpMaid()
                .get("/upload", (request, response) -> response.setJavaResourceAsBody("upload.html"))
                .post("/upload", (MultipartHandler) (request, response) -> {
                    final MultipartPart part = request.partIterator().next();
                    final String content = part.readContentToString();
                    System.out.println(content);
                })
                .configured(toExposeMultipartBodiesUsingMultipartIteratorBody())
                .build();
        //Showcase end multipartIterator

        Deployer.test(httpMaid, client -> {
            final String form = client.issue(aGetRequestToThePath("/upload").mappedToString());
            assertThat(form, containsString("<input type=\"file\" name=\"myFile\" />"));

            final HttpClientRequestBuilder<SimpleHttpResponseObject> request = aPostRequestToThePath("/upload")
                    .withAMultipartBodyWithTheParts(aPartWithTheControlName("myFile")
                            .withTheFileName("foo")
                            .withTheContent("fwerre")
                    );
            final SimpleHttpResponseObject response = client.issue(request);
            assertThat(response.getStatusCode(), is(200));
        });
    }
}
