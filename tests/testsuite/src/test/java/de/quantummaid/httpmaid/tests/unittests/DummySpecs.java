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

import org.junit.jupiter.api.Test;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static de.quantummaid.httpmaid.multipart.internal.MockHttpServletRequest.mockHttpServletRequest;
import static de.quantummaid.httpmaid.multipart.internal.SpecialServletInputStream.servletInputStreamBackedBy;
import static de.quantummaid.httpmaid.util.streams.Streams.stringToInputStream;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public final class DummySpecs {
    private static final List<String> IGNORED_METHODS = List.of("toString", "hashCode", "equals", "getClass", "wait", "notify", "notifyAll");

    private static void ensureAllMethodsThrowUnsupportedOperationException(final Object object,
                                                                           final String... excludedMethods) {
        final List<String> excludedMethodsList = asList(excludedMethods);
        final Class<?> type = object.getClass();
        for (final Method method : type.getMethods()) {
            final String name = method.getName();
            if (IGNORED_METHODS.contains(name)) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (!excludedMethodsList.contains(name)) {
                ensureMethodThrowsUnsupportedOperationException(object, method);
            }
        }
    }

    private static void ensureMethodThrowsUnsupportedOperationException(final Object object,
                                                                        final Method method) {
        final int parameterCount = method.getParameterCount();
        final Object[] parametersArray = new Object[parameterCount];
        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterCount; ++i) {
            final Class<?> parameterType = parameterTypes[i];
            parametersArray[i] = createInstanceOf(parameterType);
        }
        Throwable exception = null;
        try {
            method.invoke(object, parametersArray);
        } catch (final InvocationTargetException e) {
            exception = e.getCause();
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (final Exception e) {
            throw new RuntimeException("thrown in " + method.getName(), e);
        }
        assertThat(method.getName(), exception, instanceOf(UnsupportedOperationException.class));
    }

    private static Object createInstanceOf(final Class<?> type) {
        if (type.equals(boolean.class)) {
            return false;
        }
        if (type.equals(int.class)) {
            return 0;
        }
        if (type.isArray()) {
            final Class<?> componentType = type.getComponentType();
            return Array.newInstance(componentType, 0);
        }
        return null;
    }

    @Test
    public void testMultipartDummyServlet() {
        final HttpServletRequest object = mockHttpServletRequest(stringToInputStream(""), "");
        ensureAllMethodsThrowUnsupportedOperationException(object,
                "getContentLength",
                "getContentType",
                "getHeader",
                "getMethod",
                "getInputStream",
                "getHttpServletMapping",
                "newPushBuilder",
                "getTrailerFields",
                "isTrailerFieldsReady",
                "getCharacterEncoding"
        );
    }

    @Test
    public void testSpecialServlet() {
        final ServletInputStream object = servletInputStreamBackedBy(stringToInputStream(""));
        ensureAllMethodsThrowUnsupportedOperationException(object,
                "readLine",
                "read",
                "readAllBytes",
                "readNBytes",
                "close",
                "mark",
                "markSupported",
                "transferTo",
                "skip",
                "skipNBytes",
                "available",
                "reset"
        );
    }
}
