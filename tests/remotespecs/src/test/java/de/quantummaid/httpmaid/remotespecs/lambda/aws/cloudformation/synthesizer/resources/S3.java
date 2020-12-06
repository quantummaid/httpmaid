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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources;

import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationResource;

import java.util.List;
import java.util.Map;

public final class S3 {

    private S3() {
    }

    public static CloudformationResource bucket(final String resourceId,
                                                final String bucketName) {
        return CloudformationResource.cloudformationResource(resourceId, "AWS::S3::Bucket", Map.of(
           "BucketName", bucketName,
           "AccessControl", "Private",
                "LifecycleConfiguration", Map.of(
                        "Rules", List.of(
                                Map.of(
                                        "Status", "Enabled",
                                        "ExpirationInDays", 7
                                )
                        )
                )
        ));
    }
}
