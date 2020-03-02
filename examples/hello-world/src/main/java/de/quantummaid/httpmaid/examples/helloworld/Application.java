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

package de.quantummaid.httpmaid.examples.helloworld;

import de.quantummaid.httpmaid.HttpMaid;
import com.google.gson.Gson;
import de.quantummaid.mapmaid.MapMaid;

import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.cors.CorsConfigurators.toActivateCORSWithoutValidatingTheOrigin;
import static de.quantummaid.httpmaid.http.Http.StatusCodes.OK;
import static de.quantummaid.httpmaid.http.HttpRequestMethod.GET;
import static de.quantummaid.httpmaid.logger.LoggerConfigurators.toLogToStdout;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.toUseMapMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static de.quantummaid.mapmaid.MapMaid.aMapMaid;

public final class Application {

    private static final int PORT = 1337;

    private Application() {
    }

    public static void main(final String[] args) {
        final Gson gson = new Gson();
        final MapMaid mapMaid = aMapMaid(Application.class.getPackageName())
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .build();
        final HttpMaid httpMaid = anHttpMaid()
                .get("/api/hello", (httpRequest, httpResponse) -> {
                    final Optional<String> name = httpRequest.queryParameters().getOptionalQueryParameter("name");
                    httpResponse.setBody("Hello " + name.orElse("World"));
                    httpResponse.setStatus(OK);
                })
                .get("/api/helloUseCase", HelloWorldUseCase.class)
                .get("/api/helloDirect", metaData -> {
                    final Optional<String> name = metaData.get(QUERY_PARAMETERS).getOptionalQueryParameter("name");
                    metaData.set(REQUEST_BODY_STRING, "Hello " + name.orElse("World!"));
                    metaData.set(RESPONSE_STATUS, OK);
                })
                .configured(toActivateCORSWithoutValidatingTheOrigin()
                        .withAllowedMethods(GET)
                        .allowingCredentials())
                .configured(toUseMapMaid(mapMaid))
                .configured(toLogToStdout())
                .build();

        pureJavaEndpointFor(httpMaid).listeningOnThePort(PORT);
    }
}
