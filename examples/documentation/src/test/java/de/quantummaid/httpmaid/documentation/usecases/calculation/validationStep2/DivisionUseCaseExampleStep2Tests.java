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

package de.quantummaid.httpmaid.documentation.usecases.calculation.validationStep2;

import com.google.gson.Gson;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import de.quantummaid.httpmaid.documentation.usecases.calculation.usecases.MultiplicationUseCase;
import de.quantummaid.httpmaid.documentation.usecases.calculation.validationStep2.usecases.DivisionUseCase;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.documentation.support.curl.Curl.parseFromCurlFile;
import static de.quantummaid.httpmaid.http.headers.ContentType.json;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.toConfigureMapMaidUsingRecipe;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toMarshallContentType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class DivisionUseCaseExampleStep2Tests {
    private static final Gson GSON = new Gson();

    @Test
    public void divisionUseCaseExampleStep2() {
        //Showcase start divisionExampleStep2
        final HttpMaid httpMaid = anHttpMaid()
                .post("/multiply", MultiplicationUseCase.class)
                .post("/divide", DivisionUseCase.class)
                .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
                .configured(toConfigureMapMaidUsingRecipe(mapMaidBuilder -> {
                    mapMaidBuilder.withExceptionIndicatingValidationError(IllegalArgumentException.class);
                }))
                .build();
        //Showcase end divisionExampleStep2

        Deployer.test(httpMaid, client -> {
            assertNormalDivision(client);
            assertDivisionByZero(client);
        });
    }

    private void assertNormalDivision(final HttpMaidClient client) {
        final HttpClientRequestBuilder<SimpleHttpResponseObject> request = parseFromCurlFile("division1.curl");
        final SimpleHttpResponseObject response = client.issue(request);
        final int statusCode = response.getStatusCode();
        assertThat(statusCode, is(200));
        final String body = response.getBody();
        assertThat(body, is("{\"result\":\"4\"}"));
    }

    private void assertDivisionByZero(final HttpMaidClient client) {
        final HttpClientRequestBuilder<SimpleHttpResponseObject> request = parseFromCurlFile("division2.curl");
        final SimpleHttpResponseObject response = client.issue(request);
        final int statusCode = response.getStatusCode();
        assertThat(statusCode, is(500));
        final String body = response.getBody();
        assertThat(body, is("{\"errors\":[{\"message\":\"the divisor must not be 0\",\"path\":\"divisionRequest\"}]}"));
    }
}
