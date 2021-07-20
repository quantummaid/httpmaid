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

package de.quantummaid.httpmaid.documentation.validation;

import com.google.gson.Gson;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import de.quantummaid.httpmaid.exceptions.ExceptionConfigurators;
import de.quantummaid.mapmaid.mapper.deserialization.validation.AggregatedValidationException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aPostRequestToThePath;
import static de.quantummaid.httpmaid.http.headers.ContentType.json;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toMarshallContentType;
import static de.quantummaid.httpmaid.usecases.UseCaseConfigurators.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class AdvancedValidationExampleTests {

    @Test
    public void setValidationErrorStatusCode() {
        final Gson GSON = new Gson();
        //Showcase start setValidationErrorStatusCode
        final HttpMaid httpMaid = anHttpMaid()
                .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
                .post("/", SomeUseCase.class)
                .configured(withMapperConfiguration(mapMaidBuilder ->
                        mapMaidBuilder.withExceptionIndicatingValidationError(SomeValidationException.class)))
                .configured(toSetStatusCodeOnMapMaidValidationErrorsTo(401))
                .build();
        //Showcase end setValidationErrorStatusCode

        Deployer.test(httpMaid, httpMaidClient -> {
            final SimpleHttpResponseObject response = httpMaidClient.issue(aPostRequestToThePath("/")
                    .withContentType("application/json")
                    .withTheBody("{\"alwaysInvalidType\": {}}"));
            assertThat(response.getStatusCode(), is(401));
        });
    }

    @Test
    public void disableAggregation() {
        final Gson GSON = new Gson();
        //Showcase start disableAggregation
        final HttpMaid httpMaid = anHttpMaid()
                .post("/", SomeUseCase.class)
                .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
                .configured(withMapperConfiguration(mapMaidBuilder ->
                        mapMaidBuilder.withExceptionIndicatingValidationError(SomeValidationException.class)))
                .configured(toNotCreateAnAutomaticResponseForMapMaidValidationErrors())
                .configured(ExceptionConfigurators.toMapExceptionsOfType(AggregatedValidationException.class, (exception, request, response) -> {
                    // handle validation errors here
                }))
                .build();
        //Showcase end disableAggregation

        Deployer.test(httpMaid, httpMaidClient -> {
            final SimpleHttpResponseObject response = httpMaidClient.issue(aPostRequestToThePath("/")
                    .withContentType("")
                    .withTheBody("{\"alwaysInvalidType\": {}}"));
            assertThat(response.getStatusCode(), is(500));
        });
    }
}
