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

package de.quantummaid.httpmaid.awslambdacognitoauthorizer;

import de.quantummaid.httpmaid.chains.Configurator;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.function.Supplier;

import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.CognitoWebsocketAuthorizer.cognitoWebsocketAuthorizer;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toAuthorizeWebsocketsUsing;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toStoreAdditionalDataInWebsocketContext;

public final class CognitoConfigurators {

    private CognitoConfigurators() {
    }

    public static Configurator toStoreCognitoDataInWebsocketContext(final CognitoContextEnricher enricher) {
        validateNotNull(enricher, "enricher");
        return toStoreAdditionalDataInWebsocketContext(enricher);
    }

    public static Configurator toAuthorizeWebsocketsWithCognito(final String issuerUrl,
                                                                final String poolClientId,
                                                                final TokenExtractor tokenExtractor) {
        final Supplier<CognitoIdentityProviderClient> client = CognitoIdentityProviderClient::create;
        return toAuthorizeWebsocketsWithCognito(client, issuerUrl, poolClientId, tokenExtractor);
    }

    public static Configurator toAuthorizeWebsocketsWithCognito(final CognitoIdentityProviderClient client,
                                                                final String issuerUrl,
                                                                final String poolClientId,
                                                                final TokenExtractor tokenExtractor) {
        return toAuthorizeWebsocketsWithCognito(() -> client, issuerUrl, poolClientId, tokenExtractor);
    }

    public static Configurator toAuthorizeWebsocketsWithCognito(final Supplier<CognitoIdentityProviderClient> client,
                                                                final String issuerUrl,
                                                                final String poolClientId,
                                                                final TokenExtractor tokenExtractor) {
        return toAuthorizeWebsocketsUsing(() -> cognitoWebsocketAuthorizer(
                client.get(),
                tokenExtractor,
                issuerUrl,
                poolClientId
        ));
    }
}
