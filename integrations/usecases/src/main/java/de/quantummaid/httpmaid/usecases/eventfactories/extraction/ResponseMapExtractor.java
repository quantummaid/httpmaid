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

package de.quantummaid.httpmaid.usecases.eventfactories.extraction;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.handler.http.HttpResponse;
import de.quantummaid.httpmaid.usecases.UseCasesModule;

import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.handler.http.HttpResponse.httpResponse;

public interface ResponseMapExtractor extends Processor {

    @SuppressWarnings("unchecked")
    @Override
    default void apply(final MetaData metaData) {
        final Optional<Object> eventReturnValue = metaData.get(UseCasesModule.RECEIVED_EVENT);
        eventReturnValue.ifPresent(map -> {
            if (!(map instanceof Map)) {
                return;
            }
            final HttpResponse httpResponse = httpResponse(metaData);
            extract((Map<String, Object>) map, httpResponse);
        });
    }

    void extract(Map<String, Object> map, HttpResponse response);
}
