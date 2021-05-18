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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationOutput.cloudformationOutput;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.DynamoDb.dynamoDbTable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketRegistryModule implements CloudformationModule {
    private final Namespace namespace;
    private final CloudformationResource dynamoDb;

    public static WebsocketRegistryModule websocketRegistryModule(final Namespace namespace) {
        final CloudformationResource dynamoDb = dynamoDbTable(namespace.id("WebsocketRegistryTable"));
        return new WebsocketRegistryModule(namespace, dynamoDb);
    }

    public CloudformationResource dynamoDb() {
        return dynamoDb;
    }

    @Override
    public void apply(final CloudformationTemplateBuilder builder) {
        builder.withResources(dynamoDb);
        builder.withOutputs(cloudformationOutput(namespace.id("WebsocketRegistryDynamoDb"), dynamoDb.reference()));
    }
}
