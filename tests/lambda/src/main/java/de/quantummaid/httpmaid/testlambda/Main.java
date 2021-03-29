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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Main {

    private Main() {
    }

    public static void main(final String[] args) {
        final String entryPoint = System.getenv("CUSTOM_ENTRYPOINT");
        if (entryPoint == null) {
            throw new IllegalStateException("please supply an entry point via the CUSTOM_ENTRYPOINT environment variable");
        }
        switch (entryPoint) {
            case "DummyAuthorizer":
                log.debug("using entrypoint DummyAuthorizer");
                DummyAuthorizerGraalVmLambda.start();
                break;
            case "Unauthorized":
                log.debug("using entrypoint Unauthorized");
                UnauthorizedGraalVmLambda.start();
                break;
            case "CognitoAuthorizer":
                log.debug("using entrypoint CognitoAuthorizer");
                CognitoAuthorizerGraalVmLambda.start();
                break;
            default:
                throw new IllegalStateException("unknown entry point: " + entryPoint);
        }
    }
}
