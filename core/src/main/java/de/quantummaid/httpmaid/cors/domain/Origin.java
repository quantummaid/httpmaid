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

package de.quantummaid.httpmaid.cors.domain;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.cors.Cors;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_HEADERS;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Origin {
    private final String origin;

    public static Optional<Origin> load(final MetaData metaData) {
        Validators.validateNotNull(metaData, "metaData");
        return metaData.get(REQUEST_HEADERS).getOptionalHeader(Cors.ORIGIN).map(Origin::fromString);
    }

    public static Origin fromString(final String origin) {
        Validators.validateNotNullNorEmpty(origin, "origin");
        return new Origin(origin);
    }

    public String internalValueForMapping() {
        return origin;
    }
}
