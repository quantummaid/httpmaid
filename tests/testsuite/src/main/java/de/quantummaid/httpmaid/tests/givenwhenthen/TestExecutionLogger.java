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

package de.quantummaid.httpmaid.tests.givenwhenthen;

import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.Optional;

@Slf4j
public class TestExecutionLogger implements TestExecutionListener {
    @Override
    public void testPlanExecutionStarted(final TestPlan testPlan) {
        log.info(String.format("testPlanExecutionStarted(%s)", testPlan));
    }

    @Override
    public void testPlanExecutionFinished(final TestPlan testPlan) {
        log.info(String.format("testPlanExecutionFinished(%s)", testPlan));
    }

    private String shortened(final TestIdentifier testIdentifier) {
        final Optional<TestSource> source = testIdentifier.getSource();
        return source.map(testSource -> {
            final MethodSource methodSource = (MethodSource) testSource;
            final String className = methodSource.getClassName();
            final int lastDotIndex = className.lastIndexOf('.');
            final String shortClassName = className.substring(lastDotIndex + 1);
            return String.format("%s::%s::%s", shortClassName, methodSource.getMethodName(), testIdentifier.getDisplayName());
        })
                .orElse(testIdentifier.toString());
    }

    @Override
    public void executionSkipped(final TestIdentifier testIdentifier, final String reason) {
        log.info(String.format("executionSkipped(id:%s, reason:%s)", shortened(testIdentifier), reason));
    }

    @Override
    public void executionStarted(final TestIdentifier testIdentifier) {
        log.info(String.format("executionStarted(id:%s)", shortened(testIdentifier)));
    }

    @Override
    public void executionFinished(final TestIdentifier testIdentifier, final TestExecutionResult testExecutionResult) {
        log.info(String.format("executionFinished(id:%s, result:%s)", shortened(testIdentifier), testExecutionResult));
    }

    @Override
    public void reportingEntryPublished(final TestIdentifier testIdentifier, final ReportEntry entry) {
        log.info(String.format("reportingEntryPublished(id:%s, entry:%s)", shortened(testIdentifier), entry));
    }
}
