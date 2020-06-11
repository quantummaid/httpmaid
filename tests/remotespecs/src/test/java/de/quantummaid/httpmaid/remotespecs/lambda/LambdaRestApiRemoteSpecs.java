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

package de.quantummaid.httpmaid.remotespecs.lambda;

import de.quantummaid.httpmaid.remotespecs.RemoteSpecs;
import de.quantummaid.httpmaid.remotespecs.RemoteSpecsExtension;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import org.junit.jupiter.api.extension.ExtendWith;

import static de.quantummaid.httpmaid.remotespecs.lambda.LambdaDeployer.lambdaDeployer;

@ExtendWith(RemoteSpecsExtension.class)
public final class LambdaRestApiRemoteSpecs implements RemoteSpecs {

    @Override
    public Deployer provideDeployer() {
        return lambdaDeployer();
    }
}