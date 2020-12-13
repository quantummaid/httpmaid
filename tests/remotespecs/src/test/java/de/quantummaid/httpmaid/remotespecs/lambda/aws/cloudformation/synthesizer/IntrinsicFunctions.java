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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class IntrinsicFunctions {

    private IntrinsicFunctions() {
    }

    public static Object reference(final String resource) {
        return Map.of("Ref", resource);
    }

    public static Object sub(final String string) {
        return Map.of("Fn::Sub", string);
    }

    public static Object getAttribute(final String name, final String attribute) {
        return Map.of("Fn::GetAtt", List.of(name, attribute));
    }

    public static Object join(final String delimiter, final Object... values) {
        return Map.of("Fn::Join", List.of(delimiter, Arrays.asList(values)));
    }
}