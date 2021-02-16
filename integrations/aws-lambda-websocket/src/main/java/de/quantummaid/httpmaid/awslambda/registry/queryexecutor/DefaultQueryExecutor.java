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

package de.quantummaid.httpmaid.awslambda.registry.queryexecutor;

import de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation;
import de.quantummaid.httpmaid.awslambda.registry.EntryDeserializer;
import de.quantummaid.httpmaid.awslambda.repository.Repository;
import de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation.restore;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultQueryExecutor implements QueryExecutor {

    public static QueryExecutor defaultQueryExecutor() {
        return new DefaultQueryExecutor();
    }

    @Override
    public List<WebsocketRegistryEntry> connections(final WebsocketCriteria criteria,
                                                    final Repository repository) {
        final Map<String, Map<String, Object>> maps = repository.loadAll();
        return maps.entrySet().stream()
                .map(entry -> {
                    final String key = entry.getKey();
                    final AwsWebsocketConnectionInformation connectionInformation = restore(key);
                    return EntryDeserializer.deserializeEntry(connectionInformation, entry.getValue());
                })
                .filter(criteria::filter)
                .collect(toList());
    }
}
