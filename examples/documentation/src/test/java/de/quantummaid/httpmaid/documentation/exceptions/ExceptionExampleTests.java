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

package de.quantummaid.httpmaid.documentation.exceptions;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsByDefaultUsing;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;

public final class ExceptionExampleTests {

    @Test
    public void exceptionInHandlerExample() {
        //Showcase start exceptionInHandler
        final HttpMaid httpMaid = anHttpMaid()
                .get("/exception", (request, response) -> {
                    throw new RuntimeException("this is an example");
                })
                .build();
        //Showcase end exceptionInHandler

        Deployer.test(httpMaid, client ->
                Deployer.assertGet("/exception", "", 500, client));
    }

    @Test
    public void mappedDefaultExceptionExample() {
        //Showcase start defaultMappedException
        final HttpMaid httpMaid = anHttpMaid()
                .get("/exception", (request, response) -> {
                    throw new RuntimeException("this is an example");
                })
                .configured(toMapExceptionsByDefaultUsing((exception, request, response) -> response.setBody("Something went wrong")))
                .build();
        //Showcase end defaultMappedException

        Deployer.test(httpMaid, client ->
                Deployer.assertGet("/exception", "Something went wrong", 500, client));
    }

    @Test
    public void mappedSpecificExceptionExample() {
        //Showcase start specificMappedException
        final HttpMaid httpMaid = anHttpMaid()
                .get("/exception", (request, response) -> {
                    throw new UnsupportedOperationException("this is an example");
                })
                .configured(toMapExceptionsByDefaultUsing((exception, request, response) -> response.setBody("Something went wrong")))
                .configured(toMapExceptionsOfType(UnsupportedOperationException.class, (exception, request, response) -> response.setBody("Operation not supported")))
                .build();
        //Showcase end specificMappedException

        Deployer.test(httpMaid, client ->
                Deployer.assertGet("/exception", "Operation not supported", 500, client));
    }
}
