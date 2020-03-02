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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

import static de.quantummaid.httpmaid.marshalling.urlencoded.KeyElement.keyElement;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class Key {
    private final List<KeyElement> parts;

    static Key emptyKey() {
        return new Key(new LinkedList<>());
    }

    static Key parseKey(final String string) {
        validateNotNull(string, "string");
        if (!string.contains("[")) {
            return new Key(singletonList(keyElement(string)));
        }
        final String head = string.substring(0, string.indexOf("["));
        final String tail = string.substring(string.indexOf("[") + 1, string.length() - 1);
        final String[] tailElements = tail.split("]\\[");
        final List<KeyElement> elements = new LinkedList<>();
        elements.add(keyElement(head));
        stream(tailElements)
                .map(KeyElement::keyElement)
                .forEach(elements::add);
        return new Key(elements);
    }

    boolean isPrefixTo(final Key candidate) {
        if (this.parts.size() > candidate.parts.size()) {
            return false;
        }
        for (int i = 0; i < this.parts.size(); ++i) {
            if (!this.parts.get(i).equals(candidate.parts.get(i))) {
                return false;
            }
        }
        return true;
    }

    KeyElement elementAfter(final Key prefix) {
        if (!prefix.isPrefixTo(this)) {
            throw new IllegalArgumentException();
        }
        final int index = prefix.parts.size();
        if (index >= this.parts.size()) {
            throw new IllegalArgumentException();
        }
        return this.parts.get(index);
    }

    Key child(final String element) {
        return child(keyElement(element));
    }

    Key child(final KeyElement element) {
        validateNotNull(element, "element");
        final List<KeyElement> newParts = new LinkedList<>(this.parts);
        newParts.add(element);
        return new Key(newParts);
    }

    String render() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.parts.size(); ++i) {
            final KeyElement element = this.parts.get(i);
            final String rendered;
            if (i == 0) {
                rendered = element.renderAsFirstElement();
            } else {
                rendered = element.renderAsIndexElement();
            }
            builder.append(rendered);
        }
        return builder.toString();
    }
}
