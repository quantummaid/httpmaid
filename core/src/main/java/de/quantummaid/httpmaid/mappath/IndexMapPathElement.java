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

import java.util.List;

import static de.quantummaid.httpmaid.mappath.rendering.RenderedElement.renderedElement;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class IndexMapPathElement implements MapPathElement {
    private final int index;

    public static IndexMapPathElement indexMapPathElement(final int index) {
        return new IndexMapPathElement(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Retrieval retrieve(final Object object) {
        if (!(object instanceof List)) {
            return Retrieval.error(format("expected a List in order to retrieve index '%d' but found: %s",
                    index, object));
        }
        final List<Object> list = (List<Object>) object;
        if (index >= list.size()) {
            return Retrieval.error(format("cannot retrieve index '%d' out of List because its size is '%d'",
                    index, list.size()));
        }
        return Retrieval.success(list.get(index));
    }

    @Override
    public RenderedElement render() {
        return renderedElement(format("[%d]", index));
    }
}
