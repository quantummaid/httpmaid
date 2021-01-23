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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudwatch;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.mapmaid.shared.validators.NotNullValidator.validateNotNull;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudwatchLogGroupReference {
    private final String logGroup;
    private final String region;

    public static CloudwatchLogGroupReferenceBuilder builder() {
        return new CloudwatchLogGroupReferenceBuilder();
    }

    public static CloudwatchLogGroupReference cloudwatchLogGroupReference(final String logGroup,
                                                                          final String region) {
        validateNotNull(logGroup, "logGroup");
        validateNotNull(region, "region");
        return new CloudwatchLogGroupReference(logGroup, region);
    }

    public String buildUrl() {
        final String encodedLogGroup = encode(logGroup, UTF_8);
        return "https://"
                + region
                + ".console.aws.amazon.com/cloudwatch/home?region="
                + region
                + "#logsV2:log-groups/log-group/"
                + encodedLogGroup;
    }

    public String buildDescription() {
        final String url = buildUrl();
        return "cloudwatch logs:\nlog group: " + logGroup + "\nregion: " + region + "\nURL: " + url;
    }
}
