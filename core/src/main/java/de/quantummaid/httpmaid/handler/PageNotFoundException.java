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

package de.quantummaid.httpmaid.handler;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.http.HttpRequestMethod;
import de.quantummaid.httpmaid.path.Path;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.METHOD;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.PATH;
import static java.lang.String.format;

public final class PageNotFoundException extends RuntimeException {

    private PageNotFoundException(final String message) {
        super(message);
    }

    public static PageNotFoundException pageNotFoundException(final MetaData metaData) {
        final Path path = metaData.get(PATH);
        final HttpRequestMethod requestMethod = metaData.get(METHOD);
        return new PageNotFoundException(format(
                "No handler found for path '%s' and method '%s'%n%n%s",
                path.raw(), requestMethod.name(), metaData.prettyPrint()));
    }
}
