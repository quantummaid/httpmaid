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

package de.quantummaid.httpmaid.tests;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.handler.PageNotFoundException;
import de.quantummaid.httpmaid.tests.usecases.echobody.EchoBodyUseCase;
import de.quantummaid.httpmaid.tests.usecases.echocontenttype.EchoContentTypeUseCase;
import de.quantummaid.httpmaid.tests.usecases.responsecontenttype.SetContentTypeInResponseUseCase;
import de.quantummaid.httpmaid.tests.usecases.responseheaders.HeadersInResponseUseCase;
import de.quantummaid.httpmaid.tests.usecases.simple.TestUseCase;
import de.quantummaid.httpmaid.tests.usecases.twoparameters.TwoParametersUseCase;
import de.quantummaid.httpmaid.tests.usecases.vooooid.VoidUseCase;

import static de.quantummaid.httpmaid.Configurators.toCustomizeResponsesUsing;
import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_HEADERS;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_STATUS;
import static de.quantummaid.httpmaid.events.EventConfigurators.mappingHeader;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static de.quantummaid.httpmaid.http.Http.Headers.CONTENT_TYPE;
import static de.quantummaid.httpmaid.http.Http.StatusCodes.METHOD_NOT_ALLOWED;
import static de.quantummaid.httpmaid.http.Http.StatusCodes.OK;
import static de.quantummaid.httpmaid.http.HttpRequestMethod.*;

public final class HttpMaidTestConfigurations {

    private HttpMaidTestConfigurations() {
    }

    public static HttpMaid theHttpMaidInstanceUsedForTesting() {
        return anHttpMaid()
                .serving(TestUseCase.class).forRequestPath("/test").andRequestMethods(GET, POST, PUT, DELETE)
                .serving(EchoBodyUseCase.class).forRequestPath("/echo_body").andRequestMethods(GET, POST, PUT, DELETE)
                .get("/headers_response", HeadersInResponseUseCase.class)
                .get("/echo_contenttype", EchoContentTypeUseCase.class)
                .get("/set_contenttype_in_response", SetContentTypeInResponseUseCase.class)
                .get("/void", VoidUseCase.class)

                .configured(toMapExceptionsOfType(PageNotFoundException.class, (exception, response) -> {
                    response.setStatus(METHOD_NOT_ALLOWED);
                    response.setBody("No use case found.");
                }))
                .configured(toCustomizeResponsesUsing(metaData -> {
                    metaData.set(RESPONSE_STATUS, OK);
                    metaData.get(RESPONSE_HEADERS).put(CONTENT_TYPE, "application/json");
                }))
                .build();
    }
}
