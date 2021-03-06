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

package de.quantummaid.httpmaid.chains.rules;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.ModuleIdentifier;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Predicate;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Rule {
    private final ModuleIdentifier moduleIdentifier;
    private final Predicate<MetaData> matcher;
    private final Action action;
    private final RuleDescription ruleDescription;

    public static Rule rule(final ModuleIdentifier moduleIdentifier,
                            final Predicate<MetaData> matcher,
                            final Action action,
                            final RuleDescription ruleDescription) {
        validateNotNull(moduleIdentifier, "moduleIdentifier");
        validateNotNull(matcher, "matcher");
        validateNotNull(action, "action");
        validateNotNull(ruleDescription, "ruleDescription");
        return new Rule(moduleIdentifier, matcher, action, ruleDescription);
    }

    public boolean matches(final MetaData metaData) {
        return matcher.test(metaData);
    }

    public Action action() {
        return action;
    }

    public ModuleIdentifier moduleIdentifier() {
        return moduleIdentifier;
    }

    public RuleDescription description() {
        return ruleDescription;
    }
}
