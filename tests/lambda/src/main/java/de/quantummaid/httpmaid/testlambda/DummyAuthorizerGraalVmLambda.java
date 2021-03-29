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

package de.quantummaid.httpmaid.testlambda;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static de.quantummaid.graalvmlambdaruntime.GraalVmLambdaRuntime.startGraalVmLambdaRuntime;
import static de.quantummaid.httpmaid.lambdastructure.Structures.LAMBDA_EVENT;

@ToString
@EqualsAndHashCode
@Slf4j
public final class DummyAuthorizerGraalVmLambda {
    private final Router router = Router.router(builder -> {
    });

    public Map<String, Object> handleRequest(final Map<String, Object> event) {
        log.debug("new lambda event: {}", event);
        LAMBDA_EVENT.runValidation(event);
        return router.route(event);
    }

    @SuppressWarnings("unchecked")
    public static void start() {
        final DummyAuthorizerGraalVmLambda lambda = new DummyAuthorizerGraalVmLambda();
        startGraalVmLambdaRuntime(map -> lambda.handleRequest((Map<String, Object>) map));
    }
}
