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

package de.quantummaid.httpmaid.tests.unittests;

import de.quantummaid.httpmaid.mappath.MapPath;
import de.quantummaid.httpmaid.mappath.MapPathException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.mappath.MapPath.mapPath;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public final class MapPathSpecs {

    @Test
    public void mapPathCanBeRendered() {
        final MapPath mapPath = mapPath().key("foo").index(1337).key("bar");
        assertThat(mapPath.render(), is("foo[1337].bar"));
    }

    @Test
    public void mapPathCanBeParsed() {
        assertParsable("foo");
        assertParsable("foo.bar");
        assertParsable("foo.bar[1]");
        assertParsable("foo[1337]");
        assertParsable("foo[1337].bar");
        assertParsable("foo[1337][42]");
        assertParsable("foo[1337][42].bar");
        assertParsable("foo\tbar");
        assertParsable("foo bar");
        assertParsable("\t");
        assertParsable(" ");
        assertParsable(" .\t");
    }

    @Test
    public void errorHandling() {
        assertError("", "at position 0: key is empty");
        assertError(".", "at position 0: key is empty");
        assertError("[", "at position 0: key is empty");
        assertError("foo.", "at position 4: key is empty");
        assertError("foo..", "at position 4: key is empty");
        assertError("foo..bar", "at position 4: key is empty");
        assertError("foo[]", "at position 4: index is empty");
        assertError("foo[", "at position 4: square brackets are never closed");
        assertError("foo]", "at position 3: square brackets are closed without opening them before");
        assertError("]", "at position 0: square brackets are closed without opening them before");
        assertError("foo[bar]", "at position 4: non-digit character in array index: 'b'");
        assertError("foo[1]]", "at position 6: square brackets are closed without opening them before");
        assertError("foo[.]", "at position 4: non-digit character in array index: '.'");
        assertError("foo[[", "at position 4: square brackets are opened inside of square brackets");
        assertError("foo[1][", "at position 7: square brackets are never closed");
        assertError("foo[1[", "at position 5: square brackets are opened inside of square brackets");
        assertError("foo[-1]", "at position 4: non-digit character in array index: '-'");
        assertError("foo.[1337]", "at position 4: key is empty");
        assertError("foo\n", "at position 3: newline is not supported");
        assertError("foo[1337]bar", "at position 9: unexpected character 'b' ('.' or '[' expected)");
    }

    @Test
    public void mapPathCanBeUsedToRetrieveValuesFromMap() {
        final Map<String, Object> map = Map.of(
                "foo", List.of(
                        Map.of("a", "b"),
                        Map.of("c", "d"),
                        Map.of("e", "f")
                )
        );
        final MapPath mapPath = mapPath().key("foo").index(2).key("e");
        final Object retrieved = mapPath.retrieve(map);
        assertThat(retrieved, is("f"));
    }

    private static void assertParsable(final String path) {
        final MapPath mapPath = MapPath.parse(path);
        assertThat(mapPath.render(), is(path));
    }

    private static void assertError(final String path, final String message) {
        MapPathException exception = null;
        try {
            MapPath.parse(path);
        } catch (final MapPathException e) {
            exception = e;
        }
        assertThat(exception, is(notNullValue()));
        assertThat(exception.getMessage(), containsString(message));
    }
}
