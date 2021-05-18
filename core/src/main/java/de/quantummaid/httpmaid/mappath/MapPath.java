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

import de.quantummaid.httpmaid.mappath.rendering.RenderedElement;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.mappath.IndexMapPathElement.indexMapPathElement;
import static de.quantummaid.httpmaid.mappath.KeyMapPathElement.keyMapPathElement;
import static de.quantummaid.httpmaid.mappath.statemachine.StateMachine.runStateMachineOn;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("java:S2259")
public final class MapPath {
    private final List<MapPathElement> elements;

    public static MapPath parse(final String mapPath) {
        validateNotNull(mapPath, "mapPath");
        final List<MapPathElement> elements = runStateMachineOn(mapPath);
        return new MapPath(elements);
    }

    public static MapPath mapPath() {
        return new MapPath(List.of());
    }

    public MapPath key(final String key) {
        return extend(keyMapPathElement(key));
    }

    public MapPath index(final int index) {
        return extend(indexMapPathElement(index));
    }

    private MapPath extend(final MapPathElement element) {
        final List<MapPathElement> newElements = new ArrayList<>(elements);
        newElements.add(element);
        return new MapPath(newElements);
    }

    public Object retrieve(final Map<String, Object> map) {
        final Retrieval retrieval = retrieveOptionally(map);
        validateNotNull(retrieval, "retrieval");
        return retrieval.value();
    }

    public Retrieval retrieveOptionally(final Map<String, Object> map) {
        Object value = map;
        Retrieval retrieval = null;
        for (final MapPathElement element : elements) {
            retrieval = element.retrieve(value);
            if (retrieval.isError()) {
                break;
            }
            value = retrieval.value();
        }
        return retrieval;
    }

    public String render() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < elements.size(); ++i) {
            final MapPathElement element = elements.get(i);
            final RenderedElement renderedElement = element.render();
            if (i != 0) {
                renderedElement.connector()
                        .ifPresent(stringBuilder::append);
            }
            final String content = renderedElement.content();
            stringBuilder.append(content);
        }
        return stringBuilder.toString();
    }
}
