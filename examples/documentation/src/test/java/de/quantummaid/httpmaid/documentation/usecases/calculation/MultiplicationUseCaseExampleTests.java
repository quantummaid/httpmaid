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

package de.quantummaid.httpmaid.documentation.usecases.calculation;

import com.google.gson.Gson;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpClientRequest;
import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import de.quantummaid.httpmaid.documentation.usecases.calculation.usecases.MultiplicationUseCase;
import de.quantummaid.httpmaid.documentation.usecases.calculation.validationStep3.useCases.DivisionUseCase;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.documentation.support.curl.Curl.parseFromCurlFile;
import static de.quantummaid.httpmaid.http.headers.ContentType.json;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toMarshallContentType;
import static de.quantummaid.httpmaid.usecases.UseCaseConfigurators.withMapperConfiguration;
import static de.quantummaid.httpmaid.usecases.eventfactories.EventConfigurators.mappingQueryParameter;
import static de.quantummaid.httpmaid.usecases.eventfactories.EventConfigurators.statusCode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class MultiplicationUseCaseExampleTests {

    @Test
    public void multiplicationUseCaseWithoutMappingExample() {
        //Showcase start multiplicationUseCaseWithoutMappingExample
        final HttpMaid httpMaid = anHttpMaid()
                .post("/multiply", MultiplicationUseCase.class)
                .build();
        //Showcase end multiplicationUseCaseWithoutMappingExample

        Deployer.test(httpMaid, client -> {
            final HttpClientRequestBuilder<SimpleHttpResponseObject> request = parseFromCurlFile("multiply1.curl");

            final SimpleHttpResponseObject response = client.issue(request);
            final int statusCode = response.getStatusCode();
            assertThat(statusCode, is(500));
            final String body = response.getBody();
            assertThat(body, is(""));
        });
    }

    @Test
    public void multiplicationUseCaseWithMappingExample() {
        //Showcase start multiplicationUseCaseWithMappingExample
        final Gson GSON = new Gson();
        final HttpMaid httpMaid = anHttpMaid()
                .post("/multiply", MultiplicationUseCase.class)
                .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
                .build();
        //Showcase end multiplicationUseCaseWithMappingExample

        Deployer.test(httpMaid, client -> {
            final HttpClientRequestBuilder<SimpleHttpResponseObject> request = parseFromCurlFile("multiply2.curl");
            final SimpleHttpResponseObject response = client.issue(request);
            final int statusCode = response.getStatusCode();
            assertThat(statusCode, is(200));
            final String body = response.getBody();
            assertThat(body, is("{\"result\":12}"));

            Deployer.assertPost("/multiply", "{\"multiplicationRequest\": {\"factor1\": \"3\", \"factor2\": \"4\"}}", "{\"result\":12}", client);
            Deployer.assertPost("/multiply", "{\"multiplicationRequest\": {\"factor1\": \"3\", \"factor2\": \"5\"}}", "{\"result\":15}", client);
            Deployer.assertPost("/multiply", "{\"multiplicationRequest\": {\"factor1\": \"20\", \"factor2\": \"7\"}}", "{\"result\":140}", client);
        });
    }

    @Test
    public void multiplicationAndDivisionUseCaseExample() {
        //Showcase start multAndDivUseCaseExample
        final Gson GSON = new Gson();
        final HttpMaid httpMaid = anHttpMaid()
                .post("/multiply", MultiplicationUseCase.class)
                .post("/divide", DivisionUseCase.class)
                .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
                .build();
        //Showcase end multAndDivUseCaseExample

        Deployer.test(httpMaid, client -> {
            Deployer.assertPost("/multiply", "{\"multiplicationRequest\": {\"factor1\": \"3\", \"factor2\": \"4\"}}", "{\"result\":12}", client);
            Deployer.assertPost("/multiply", "{\"multiplicationRequest\": {\"factor1\": \"3\", \"factor2\": \"5\"}}", "{\"result\":15}", client);
            Deployer.assertPost("/multiply", "{\"multiplicationRequest\": {\"factor1\": \"20\", \"factor2\": \"7\"}}", "{\"result\":140}", client);

            Deployer.assertPost("/divide", "{\"divisionRequest\": {\"dividend\": \"12\", \"divisor\": \"4\"}}", "{\"result\":3}", client);
            Deployer.assertPost("/divide", "{\"divisionRequest\": {\"dividend\": \"15\", \"divisor\": \"5\"}}", "{\"result\":3}", client);
            Deployer.assertPost("/divide", "{\"divisionRequest\": {\"dividend\": \"140\", \"divisor\": \"7\"}}", "{\"result\":20}", client);
        });
    }

    @Test
    public void calculationWithQueryParametersExample() {
        final Gson GSON = new Gson();
        //Showcase start calculationWithQueryParametersExample
        final HttpMaid httpMaid = anHttpMaid()
                .get("/multiply", MultiplicationUseCase.class,
                        mappingQueryParameter("factor1", "multiplicationRequest.factor1"),
                        mappingQueryParameter("factor2", "multiplicationRequest.factor2"),
                        statusCode(200)
                )
                .get("/divide", DivisionUseCase.class,
                        mappingQueryParameter("dividend", "divisionRequest.dividend"),
                        mappingQueryParameter("divisor", "divisionRequest.divisor")
                )
                .configured(toMarshallContentType(json(), string -> GSON.fromJson(string, Map.class), GSON::toJson))
                .configured(withMapperConfiguration(mapMaidBuilder -> {
                    mapMaidBuilder.withExceptionIndicatingValidationError(IllegalArgumentException.class);
                }))
                .build();
        //Showcase end calculationWithQueryParametersExample

        Deployer.test(httpMaid, client -> {
            final HttpClientRequestBuilder<SimpleHttpResponseObject> request = HttpClientRequest.aGetRequestToThePath("/multiply")
                    .withQueryParameter("factor1", "3")
                    .withQueryParameter("factor2", "4");
            final SimpleHttpResponseObject response = client.issue(request);
            final int statusCode = response.getStatusCode();
            assertThat(statusCode, is(200));

            Deployer.assertGet("/multiply?factor1=3&factor2=4", "{\"result\":12}", client);
            Deployer.assertGet("/multiply?factor1=3&factor2=5", "{\"result\":15}", client);
            Deployer.assertGet("/multiply?factor1=20&factor2=7", "{\"result\":140}", client);

            Deployer.assertGet("/divide?dividend=12&divisor=4", "{\"result\":3}", client);
            Deployer.assertGet("/divide?dividend=15&divisor=5", "{\"result\":3}", client);
            Deployer.assertGet("/divide?dividend=140&divisor=7", "{\"result\":20}", client);
        });
    }
}
