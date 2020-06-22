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

package de.quantummaid.httpmaid.tests.specs.headers;

import com.google.gson.Gson;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.toConfigureMapMaidUsingRecipe;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;

public final class ContentTypeSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void handlerCanExplicitlySetResponseContentType(final TestEnvironment testEnvironment) {
        final Gson gson = new Gson();
        final HttpMaid build = anHttpMaid()
                .post("/", (request, response) -> {
                    final Map<String, Object> map = request.bodyMap();
                    response.setBody(map);
                    response.setContentType("application/yaml");
                })
                .configured(toConfigureMapMaidUsingRecipe(mapMaidBuilder -> mapMaidBuilder
                        .withAdvancedSettings(advancedBuilder -> {
                            advancedBuilder.usingJsonMarshaller(gson::toJson, input -> gson.fromJson(input, Object.class));
                            advancedBuilder.usingYamlMarshaller(object -> "asdf", input -> {
                                throw new UnsupportedOperationException();
                            });
                        })))
                .build();
        testEnvironment.given(
                build
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{\"foo\": \"bar\"}")
                .withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("asdf");
    }
}
