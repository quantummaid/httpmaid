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

package de.quantummaid.httpmaid.dagger.factoryfinding;

public interface NamePattern {

    static NamePattern defaultFactoryClassPattern() {
        return type -> {
            final String className = type.getSimpleName();
            final String packageName = type.getPackage().getName();
            return packageName + ".Dagger" + className + "Factory";
        };
    }

    static NamePattern defaultFactoryMethodPattern() {
        return type -> {
            final String className = type.getSimpleName();
            return Character.toLowerCase(className.charAt(0)) + className.substring(1);
        };
    }

    String fromClass(Class<?> type);
}
