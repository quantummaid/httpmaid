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

package de.quantummaid.httpmaid.marshalling.urlencoded;

import de.quantummaid.httpmaid.marshalling.Unmarshaller;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.marshalling.urlencoded.ParsedUrlEncoded.parse;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UrlEncodedUnmarshaller implements Unmarshaller {

    public static Unmarshaller urlEncodedUnmarshaller() {
        return new UrlEncodedUnmarshaller();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> unmarshall(final String string) {
        final ParsedUrlEncoded parsed = parse(string);
        return (Map<String, Object>) parseFromKey(Key.emptyKey(), parsed);
    }

    private Object parseFromKey(final Key key, final ParsedUrlEncoded parsed) {
        return parsed.getValue(key)
                .map(string -> (Object) string)
                .orElseGet(() -> parseArrayOrMap(key, parsed));
    }

    private Object parseArrayOrMap(final Key key, final ParsedUrlEncoded parsed) {
        final List<KeyElement> children = parsed.directChildren(key);
        if (areArrayIndeces(children)) {
            return parseArray(key, children, parsed);
        }
        return parseMap(key, children, parsed);
    }

    private List<Object> parseArray(final Key key,
                                    final List<KeyElement> children,
                                    final ParsedUrlEncoded parsed) {
        final List<Object> list = new LinkedList<>();
        children.forEach(child -> {
            final Object childValue = parseFromKey(key.child(child), parsed);
            list.add(child.asArrayIndex(), childValue);
        });
        return list;
    }

    private Map<String, Object> parseMap(final Key key,
                                         final List<KeyElement> children,
                                         final ParsedUrlEncoded parsed) {
        final Map<String, Object> map = new HashMap<>();
        children.forEach(child -> {
            final Object childValue = parseFromKey(key.child(child), parsed);
            map.put(child.value(), childValue);
        });
        return map;
    }

    private static boolean areArrayIndeces(final List<KeyElement> indeces) {
        return indeces.stream()
                .anyMatch(KeyElement::isArrayIndex);
    }
}
