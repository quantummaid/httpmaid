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

package de.quantummaid.httpmaid.remotespecs.lambda.junit;

import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class Listener implements TestExecutionListener {
    private static final String JUPITER_ID = "[engine:junit-jupiter]";
    private static final List<Class<?>> CLASS_SOURCES = new ArrayList<>();

    @Override
    public void testPlanExecutionStarted(final TestPlan testPlan) {
        final TestIdentifier jupiterTestSuite = testPlan.getTestIdentifier(JUPITER_ID);
        final Set<TestIdentifier> children = testPlan.getChildren(jupiterTestSuite);
        children.stream()
                .map(TestIdentifier::getSource)
                .flatMap(Optional::stream)
                .map(testSource -> (ClassSource) testSource)
                .map(ClassSource::getJavaClass)
                .forEach(CLASS_SOURCES::add);
    }

    public static List<Class<?>> getClassSources() {
        return CLASS_SOURCES;
    }
}
