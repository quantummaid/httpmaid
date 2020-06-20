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

package de.quantummaid.httpmaid.awslambda;

import java.util.Map;

public final class LambdaEventException extends IllegalArgumentException {

    private LambdaEventException(final String message) {
        super(message);
    }

    public static LambdaEventException emptyLambdaEventException() {
        final String message = "Lambda event must not be empty. " +
                "Please check the way this lambda has been deployed. " +
                "If it is an HTTP lambda, make sure it is integrated in API Gateway " +
                "as type 'LAMBDA_PROXY' (not 'LAMBDA').";
        return new LambdaEventException(message);
    }

    public static LambdaEventException unknownKeyException(final String key, final Map<String, Object> map) {
        final String message = String.format("Can't find key '%s' in lambda event %s", key, map);
        return new LambdaEventException(message);
    }

    public static LambdaEventException wrongTypeException(final String key,
                                                          final Class<?> expectedType,
                                                          final Object actualValue,
                                                          final Map<String, Object> event) {
        final String message = String.format("Expected lambda event field '%s' to be of type '%s' but was '%s'. Whole event: %s",
                key, expectedType.getSimpleName(), actualValue, event);
        return new LambdaEventException(message);
    }
}
