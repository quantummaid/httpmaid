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

package de.quantummaid.httpmaid.documentation.xx_usecases.calculation.validationStep3;

import com.google.gson.Gson;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.documentation.xx_usecases.calculation.usecases.MultiplicationUseCase;
import de.quantummaid.httpmaid.documentation.xx_usecases.calculation.validationStep3.useCases.DivisionUseCase;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.http.headers.ContentType.json;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.toConfigureMapMaidUsingRecipe;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toMarshallContentType;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public final class DivisionUseCaseExampleStep3 {
    private static final Gson GSON = new Gson();

    @SuppressWarnings("unchecked")
    public static void main(final String[] args) {
        //Showcase start divisionExampleStep3
        final HttpMaid httpMaid = anHttpMaid()
                .post("/multiply", MultiplicationUseCase.class)
                .post("/divide", DivisionUseCase.class)
                .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
                .configured(toConfigureMapMaidUsingRecipe((mapMaidBuilder, dependencyRegistry) -> {
                    mapMaidBuilder.withExceptionIndicatingValidationError(IllegalArgumentException.class);
                }))
                .build();
        //Showcase end divisionExampleStep3
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
