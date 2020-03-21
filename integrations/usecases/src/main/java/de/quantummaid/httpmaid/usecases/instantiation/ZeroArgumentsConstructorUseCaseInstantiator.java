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

package de.quantummaid.httpmaid.usecases.instantiation;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.usecases.instantiation.InstantiationInformation.instantiationInformationFor;
import static de.quantummaid.httpmaid.usecases.instantiation.ZeroArgumentsConstructorUseCaseInstantiatorException.zeroArgumentsConstructorUseCaseInstantiatorException;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class ZeroArgumentsConstructorUseCaseInstantiator implements UseCaseInstantiator {
    private final Map<Class<?>, InstantiationInformation> instantiationInformations = new HashMap<>();

    public static ZeroArgumentsConstructorUseCaseInstantiator zeroArgumentsConstructorUseCaseInstantiator() {
        return new ZeroArgumentsConstructorUseCaseInstantiator();
    }

    @Override
    public <T> T instantiate(final Class<T> type) {
        if (!instantiationInformations.containsKey(type)) {
            final InstantiationInformation instantiationInformation = instantiationInformationFor(type);
            instantiationInformations.put(type, instantiationInformation);
        }
        final InstantiationInformation instantiationInformation = instantiationInformations.get(type);
        final Constructor<?> constructor = instantiationInformation.constructor();
        try {
            @SuppressWarnings("unchecked") final T newInstance = (T) constructor.newInstance();
            return newInstance;
        } catch (final ExceptionInInitializerError e) {
            final Throwable cause = e.getException();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(e);
            }
        } catch (final InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw zeroArgumentsConstructorUseCaseInstantiatorException(type, e);
        }
    }

}
