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

public final class DynamoDbMarshaller {

    private DynamoDbMarshaller() {
    }

    public static Map<String, AttributeValue> marshalTopLevelMap(final Map<String, Object> map) {
        final Map<String, AttributeValue> attributeValueMap = new HashMap<>(map.size());
        map.forEach((key, value) -> {
            final AttributeValue marshalledValue = marshal(value);
            attributeValueMap.put(key, marshalledValue);
        });
        return attributeValueMap;
    }

    @SuppressWarnings("unchecked")
    private static AttributeValue marshal(final Object object) {
        if (object instanceof String) {
            return marshalString((String) object);
        }
        if (object instanceof Map) {
            return marshalMap((Map<String, Object>) object);
        }
        throw new UnsupportedOperationException();
    }

    private static AttributeValue marshalString(final String string) {
        return AttributeValue.builder()
                .s(string)
                .build();
    }

    private static AttributeValue marshalMap(final Map<String, Object> map) {
        final Map<String, AttributeValue> attributeValueMap = marshalTopLevelMap(map);
        return AttributeValue.builder()
                .m(attributeValueMap)
                .build();
    }
}
