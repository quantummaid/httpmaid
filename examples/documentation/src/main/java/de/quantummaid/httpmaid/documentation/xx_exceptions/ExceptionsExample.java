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

package de.quantummaid.httpmaid.documentation.xx_exceptions;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.mapmaid.MapMaid;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.toUseMapMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static de.quantummaid.mapmaid.MapMaid.aMapMaid;
import static de.quantummaid.mapmaid.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe.urlEncodedMarshaller;

public final class ExceptionsExample {

    public static void main(final String[] args) {
        final MapMaid mapMaid = aMapMaid()
                .usingRecipe(urlEncodedMarshaller())
                .build();
        final HttpMaid httpMaid = anHttpMaid()
                .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
                .post("/submit", (request, response) -> {
                    final Map<String, Object> bodyMap = request.bodyMap();
                    final String name = (String) bodyMap.get("name");
                    final String profession = (String) bodyMap.get("profession");
                    response.setBody("Hello " + name + " and good luck as a " + profession + "!");
                })
                .configured(toUseMapMaid(mapMaid))
                .build();
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
