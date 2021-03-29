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

package de.quantummaid.httpmaid.testjar;

import com.oracle.svm.core.annotate.AutomaticFeature;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.remotespecsinstance.HttpMaidFactory;
import de.quantummaid.reflectmaid.ReflectMaid;
import de.quantummaid.reflectmaid.resolvedtype.ResolvedType;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.util.Collection;

@AutomaticFeature
public final class RuntimeReflectionRegistrationFeature implements Feature {

    @Override
    public void beforeAnalysis(final BeforeAnalysisAccess access) {
        final HttpMaid httpMaid = HttpMaidFactory.httpMaid();
        final ReflectMaid reflectMaid = httpMaid.reflectMaid();
        final Collection<ResolvedType> registeredTypes = reflectMaid.registeredTypes();
        registeredTypes.stream()
                .map(ResolvedType::assignableType)
                .distinct()
                .forEach(this::registerClass);
    }

    private void registerClass(final Class<?> clazz) {
        RuntimeReflection.register(clazz);
        RuntimeReflection.register(clazz.getMethods());
        RuntimeReflection.register(clazz.getConstructors());
        RuntimeReflection.register(clazz.getFields());
    }
}
