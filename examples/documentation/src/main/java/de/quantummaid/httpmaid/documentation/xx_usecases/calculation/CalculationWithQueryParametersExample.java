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

package de.quantummaid.httpmaid.documentation.xx_usecases.calculation;

import de.quantummaid.httpmaid.documentation.xx_usecases.calculation.domain.MultiplicationRequest;
import de.quantummaid.httpmaid.documentation.xx_usecases.calculation.usecases.DivisionUseCase;
import de.quantummaid.httpmaid.documentation.xx_usecases.calculation.usecases.MultiplicationUseCase;
import de.quantummaid.httpmaid.HttpMaid;
import com.google.gson.Gson;
import de.quantummaid.mapmaid.MapMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.events.EventConfigurators.toEnrichTheIntermediateMapWithAllQueryParameters;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.toUseMapMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static de.quantummaid.mapmaid.MapMaid.aMapMaid;

public final class CalculationWithQueryParametersExample {

    public static void main(final String[] args) {
        final Gson gson = new Gson();
        final MapMaid mapMaid = aMapMaid(MultiplicationRequest.class.getPackageName())
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .withExceptionIndicatingValidationError(IllegalArgumentException.class)
                .build();

        final HttpMaid httpMaid = anHttpMaid()
                .get("/multiply", MultiplicationUseCase.class)
                .get("/divide", DivisionUseCase.class)
                .configured(toEnrichTheIntermediateMapWithAllQueryParameters())
                .configured(toUseMapMaid(mapMaid))
                .build();
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
