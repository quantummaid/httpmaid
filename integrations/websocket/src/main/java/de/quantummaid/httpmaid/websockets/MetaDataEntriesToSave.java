/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid.websockets;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.MetaDataEntryProvider.saving;
import static de.quantummaid.httpmaid.websockets.SavedMetaDataEntries.savedMetaDataEntries;
import static java.util.Collections.addAll;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetaDataEntriesToSave {
    private final List<MetaDataEntryProvider<?>> entryProviders;

    public static MetaDataEntriesToSave metaDataEntriesToSave(final List<MetaDataEntryProvider<?>> entryProviders) {
        validateNotNull(entryProviders, "entryProviders");
        final List<MetaDataEntryProvider<?>> realProviders = new LinkedList<>(entryProviders);
        addAll(realProviders,
                saving(PATH),
                saving(PATH_PARAMETERS),
                saving(QUERY_PARAMETERS),
                saving(REQUEST_HEADERS),
                saving(REQUEST_CONTENT_TYPE));
        return new MetaDataEntriesToSave(realProviders);
    }

    @SuppressWarnings("unchecked")
    public SavedMetaDataEntries save(final MetaData metaData) {
        validateNotNull(metaData, "metaData");
        final Map<MetaDataKey<?>, Object> savedMetaData = new HashMap<>();
        entryProviders.forEach(entryProvider -> {
            final MetaDataEntry<?> entry = entryProvider.provide(metaData);
            savedMetaData.put(entry.key(), entry.value());
        });
        return savedMetaDataEntries(savedMetaData);
    }
}
