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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules;

import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationResource;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationTemplateBuilder;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.Namespace;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.Cognito;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationOutput.cloudformationOutput;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CognitoModule implements CloudformationModule {
    private final Namespace namespace;
    private final CloudformationResource pool;
    private final CloudformationResource poolClient;

    public static CognitoModule cognitoModule(final Namespace namespace) {
        final CloudformationResource pool = Cognito.pool(namespace.id("Pool"));
        final CloudformationResource poolClient = Cognito.poolClient(namespace.id("PoolClient"), pool);
        return new CognitoModule(namespace, pool, poolClient);
    }

    public CloudformationResource pool() {
        return pool;
    }

    public CloudformationResource poolClient() {
        return poolClient;
    }

    @Override
    public void apply(final CloudformationTemplateBuilder builder) {
        builder.withResources(pool, poolClient);
        builder.withOutputs(
                cloudformationOutput(namespace.id("PoolId"), pool.reference()),
                cloudformationOutput(namespace.id("PoolClientId"), poolClient.reference())
        );
    }
}
