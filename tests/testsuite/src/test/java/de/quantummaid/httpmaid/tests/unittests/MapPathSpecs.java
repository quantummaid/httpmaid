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
import de.quantummaid.httpmaid.mappath.Retrieval;
import org.junit.jupiter.api.Test;

import java.util.*;

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
    public void parseErrorHandling() {
        assertParseError("", "at position 0: key is empty");
        assertParseError(".", "at position 0: key is empty");
        assertParseError("[", "at position 0: key is empty");
        assertParseError("foo.", "at position 4: key is empty");
        assertParseError("foo..", "at position 4: key is empty");
        assertParseError("foo..bar", "at position 4: key is empty");
        assertParseError("foo[]", "at position 4: index is empty");
        assertParseError("foo[", "at position 4: square brackets are never closed");
        assertParseError("foo]", "at position 3: square brackets are closed without opening them before");
        assertParseError("]", "at position 0: square brackets are closed without opening them before");
        assertParseError("foo[bar]", "at position 4: non-digit character in array index: 'b'");
        assertParseError("foo[1]]", "at position 6: square brackets are closed without opening them before");
        assertParseError("foo[.]", "at position 4: non-digit character in array index: '.'");
        assertParseError("foo[[", "at position 4: square brackets are opened inside of square brackets");
        assertParseError("foo[1][", "at position 7: square brackets are never closed");
        assertParseError("foo[1[", "at position 5: square brackets are opened inside of square brackets");
        assertParseError("foo[-1]", "at position 4: non-digit character in array index: '-'");
        assertParseError("foo.[1337]", "at position 4: key is empty");
        assertParseError("foo\n", "at position 3: newline is not supported");
        assertParseError("foo[1337]bar", "at position 9: unexpected character 'b' ('.' or '[' expected)");
    }

    @Test
    public void mapPathCanBeUsedForRetrieval() {
        assertRetrievable("foo", mapOf("foo", null), null);
        assertRetrievable("foo[0]", mapOf("foo", listOf(null, null)), null);
        assertRetrievable("foo[2].e", Map.of(
                "foo", List.of(
                        Map.of("a", "b"),
                        Map.of("c", "d"),
                        Map.of("e", "f")
                )
        ), "f");
    }

    @Test
    public void retrieveErrorHandling() {
        assertRetrieveError("foo", null, "expected a Map in order to retrieve key 'foo' but found: null");
        assertRetrieveError("foo", Map.of(), "did not find key 'foo' in Map");
        assertRetrieveError("foo.bar", mapOf("foo", null), "expected a Map in order to retrieve key 'bar' but found: null");
        assertRetrieveError("foo[0]", mapOf("foo", null), "expected a List in order to retrieve index '0' but found: null");
        assertRetrieveError("foo[0]", Map.of("foo", List.of()), "cannot retrieve index '0' out of List because its size is '0'");
        assertRetrieveError("foo[0].e", Map.of("foo", listOf(null, "bar")), "expected a Map in order to retrieve key 'e' but found: null");
    }

    private static void assertParsable(final String path) {
        final MapPath mapPath = MapPath.parse(path);
        assertThat(mapPath.render(), is(path));
    }

    private static void assertParseError(final String path, final String message) {
        MapPathException exception = null;
        try {
            MapPath.parse(path);
        } catch (final MapPathException e) {
            exception = e;
        }
        assertThat(exception, is(notNullValue()));
        assertThat(exception.getMessage(), containsString(message));
    }

    private static void assertRetrievable(final String path,
                                          final Map<String, Object> map,
                                          final Object result) {
        final MapPath mapPath = MapPath.parse(path);
        final Object actual = mapPath.retrieve(map);
        assertThat(actual, is(result));
    }

    private static void assertRetrieveError(final String path,
                                            final Map<String, Object> map,
                                            final String message) {
        final MapPath mapPath = MapPath.parse(path);
        MapPathException exception = null;
        try {
            mapPath.retrieve(map);
        } catch (final MapPathException e) {
            exception = e;
        }
        assertThat(exception, is(notNullValue()));
        assertThat(exception.getMessage(), containsString(message));

        final Retrieval retrieval = mapPath.retrieveOptionally(map);
        assertThat(retrieval.isError(), is(true));
    }

    private static Map<String, Object> mapOf(final String key, final Object value) {
        final Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private static List<Object> listOf(final Object... values) {
        final List<Object> list = new ArrayList<>(values.length);
        list.addAll(Arrays.asList(values));
        return list;
    }
}
