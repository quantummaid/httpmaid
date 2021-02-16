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

package de.quantummaid.httpmaid.websocketregistryspecs.inmemory;

import de.quantummaid.httpmaid.awslambda.repository.Repository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class InMemoryRepository implements Repository {
    private final Map<String, Map<String, Object>> map;

    public static Repository inMemoryRepository() {
        return new InMemoryRepository(new LinkedHashMap<>());
    }

    @Override
    public void store(final String key, final Map<String, Object> value) {
        map.put(key, value);
    }

    @Override
    public void delete(final String key) {
        map.remove(key);
    }

    @Override
    public Map<String, Object> load(final String key) {
        return map.get(key);
    }

    @Override
    public Map<String, Map<String, Object>> loadAll() {
        return map;
    }

    @Override
    public void close() {
        // do nothing
    }
}
