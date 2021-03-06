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

package de.quantummaid.httpmaid.util;

import static de.quantummaid.httpmaid.util.CustomTypeValidationException.customTypeValidationException;

public final class Validators {

    private Validators() {
    }

    public static void validateNotNull(final Object value, final String name) {
        if (value == null) {
            throw customTypeValidationException(name + " must not be null");
        }
    }

    public static void validateNotNullNorEmpty(final String value, final String name) {
        validateNotNull(value, name);
        if (value.trim().isEmpty()) {
            throw customTypeValidationException(name + " must not be empty");
        }
    }

    public static <T> void validateArrayNeitherNullNorEmptyNorContainsNull(final T[] array, final String name) {
        validateNotNull(array, name);
        if (array.length == 0) {
            throw customTypeValidationException(name + " must not be empty");
        }
        for (int i = 0; i < array.length; ++i) {
            validateNotNull(array[i], name + "[" + i + "]");
        }
    }
}
