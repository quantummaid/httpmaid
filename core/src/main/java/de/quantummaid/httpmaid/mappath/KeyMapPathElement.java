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

import java.util.Map;

import static de.quantummaid.httpmaid.mappath.rendering.RenderedElement.renderedElement;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyMapPathElement implements MapPathElement {
    private final String key;

    public static KeyMapPathElement keyMapPathElement(final String key) {
        return new KeyMapPathElement(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Retrieval retrieve(final Object object) {
        if (!(object instanceof Map)) {
            return Retrieval.error(format("expected a Map in order to retrieve key '%s' but found: %s",
                    key, object));
        }
        final Map<String, Object> map = (Map<String, Object>) object;
        if (!map.containsKey(key)) {
            return Retrieval.error(format("did not find key '%s' in Map", key));
        }
        final Object value = map.get(key);
        return Retrieval.success(value);
    }

    @Override
    public RenderedElement render() {
        return renderedElement(key, ".");
    }
}
