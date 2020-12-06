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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationTemplate.cloudformationTemplate;

public final class CloudformationTemplateBuilder {
    private final List<CloudformationResource> resources = new ArrayList<>();
    private final List<CloudformationOutput> outputs = new ArrayList<>();

    public static CloudformationTemplateBuilder cloudformationTemplateBuilder() {
        return new CloudformationTemplateBuilder();
    }

    public CloudformationTemplateBuilder withModule(final CloudformationModule module) {
        module.apply(this);
        return this;
    }

    public CloudformationTemplateBuilder withResources(final CloudformationResource... resources) {
        this.resources.addAll(Arrays.asList(resources));
        return this;
    }

    public CloudformationTemplateBuilder withOutputs(final CloudformationOutput... outputs) {
        this.outputs.addAll(Arrays.asList(outputs));
        return this;
    }

    public CloudformationTemplate build() {
        return cloudformationTemplate(resources, outputs);
    }
}
