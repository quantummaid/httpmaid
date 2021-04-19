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

package de.quantummaid.httpmaid.mappath;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.httpmaid.mappath.MapPathException.retrievalException;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Retrieval {
    private final Object value;
    private final String error;

    public static Retrieval success(final Object value) {
        return new Retrieval(value, null);
    }

    public static Retrieval error(final String error) {
        validateNotNull(error, "error");
        return new Retrieval(null, error);
    }

    public boolean isError() {
        return error != null;
    }

    public Object value() {
        if (isError()) {
            throw retrievalException(error);
        }
        return value;
    }
}
