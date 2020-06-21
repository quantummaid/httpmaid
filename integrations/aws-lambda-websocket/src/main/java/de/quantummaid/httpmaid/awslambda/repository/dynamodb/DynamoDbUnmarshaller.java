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

package de.quantummaid.httpmaid.awslambda.repository.dynamodb;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public final class DynamoDbUnmarshaller {

    private DynamoDbUnmarshaller() {
    }

    public static Map<String, Object> unmarshalMap(final Map<String, AttributeValue> input) {
        final Map<String, Object> result = new HashMap<>(input.size());
        input.keySet().forEach(key -> {
            final AttributeValue attributeValue = input.get(key);
            final Object value = unmarshal(attributeValue);
            result.put(key, value);
        });
        return result;
    }

    private static Object unmarshal(final AttributeValue attributeValue) {
        if (attributeValue.hasM()) {
            final Map<String, AttributeValue> map = attributeValue.m();
            return unmarshalMap(map);
        }
        final Boolean nul = attributeValue.nul();
        if (nul != null && nul) {
            return null;
        }
        if (attributeValue.s() != null) {
            return attributeValue.s();
        }
        if (attributeValue.l() != null) {
            return attributeValue.l().stream()
                    .map(DynamoDbUnmarshaller::unmarshal)
                    .collect(toList());
        }
        throw new UnsupportedOperationException("Unable to unmarshal from: " + attributeValue);
    }
}
