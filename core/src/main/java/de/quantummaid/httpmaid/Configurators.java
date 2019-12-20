/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid;

import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.httpmaid.responsetemplate.ResponseTemplate;
import de.quantummaid.httpmaid.util.Validators;

public final class Configurators {

    private Configurators() {
    }

    public static Configurator toCustomizeResponsesUsing(final ResponseTemplate responseTemplate) {
        Validators.validateNotNull(responseTemplate, "responseTemplate");
        return dependencyRegistry -> {
            final CoreModule coreModule = dependencyRegistry.getDependency(CoreModule.class);
            coreModule.setResponseTemplate(responseTemplate);
        };
    }
}
