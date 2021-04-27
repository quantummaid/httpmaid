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

package de.quantummaid.httpmaid.lambdastructure;

import de.quantummaid.httpmaid.lambdastructure.validators.StructureValidator;

import static de.quantummaid.httpmaid.lambdastructure.validators.BooleanValidator.bool;
import static de.quantummaid.httpmaid.lambdastructure.validators.EmptyMapValidator.emptyMap;
import static de.quantummaid.httpmaid.lambdastructure.validators.FixedValidator.fixed;
import static de.quantummaid.httpmaid.lambdastructure.validators.ListValidator.listOf;
import static de.quantummaid.httpmaid.lambdastructure.validators.MapValidator.map;
import static de.quantummaid.httpmaid.lambdastructure.validators.NullValidator.nullValue;
import static de.quantummaid.httpmaid.lambdastructure.validators.NumberValidator.number;
import static de.quantummaid.httpmaid.lambdastructure.validators.NumericValidator.numeric;
import static de.quantummaid.httpmaid.lambdastructure.validators.OneOfValidator.oneOf;
import static de.quantummaid.httpmaid.lambdastructure.validators.StringMapValidator.mapOf;
import static de.quantummaid.httpmaid.lambdastructure.validators.StringValidator.string;

public final class Structures {

    public static final StructureValidator REST_REQUEST = map()
            .key("resource", string())
            .key("path", string())
            .key("httpMethod", oneOf(
                    fixed("GET"),
                    fixed("POST"),
                    fixed("PUT"),
                    fixed("DELETE"),
                    fixed("OPTIONS")
            ))
            .key("headers", mapOf(string()))
            .key("multiValueHeaders", mapOf(listOf(string())))
            .key("queryStringParameters", oneOf(
                    nullValue(),
                    mapOf(string())
            ))
            .key("multiValueQueryStringParameters", oneOf(
                    nullValue(),
                    mapOf(listOf(string()))
            ))
            .key("pathParameters", oneOf(
                    nullValue(),
                    mapOf(string())
            ))
            .key("stageVariables", nullValue())
            .key("requestContext", map()
                    .key("resourceId", string())
                    .key("resourcePath", string())
                    .key("httpMethod", oneOf(
                            fixed("GET"),
                            fixed("POST"),
                            fixed("PUT"),
                            fixed("DELETE"),
                            fixed("OPTIONS")
                    ))
                    .key("extendedRequestId", string())
                    .key("requestTime", string())
                    .key("path", string())
                    .key("accountId", numeric())
                    .key("protocol", fixed("HTTP/1.1"))
                    .key("stage", string())
                    .key("domainPrefix", string())
                    .key("requestTimeEpoch", number())
                    .key("requestId", string())
                    .key("identity", map()
                            .key("cognitoIdentityPoolId", nullValue())
                            .key("accountId", nullValue())
                            .key("cognitoIdentityId", nullValue())
                            .key("caller", nullValue())
                            .key("sourceIp", string())
                            .key("principalOrgId", nullValue())
                            .key("accessKey", nullValue())
                            .key("cognitoAuthenticationType", nullValue())
                            .key("cognitoAuthenticationProvider", nullValue())
                            .key("userArn", nullValue())
                            .key("userAgent", fixed("Amazon CloudFront"))
                            .key("user", nullValue())
                    )
                    .key("domainName", string())
                    .key("apiId", string())
            )
            .key("body", oneOf(
                    nullValue(),
                    string()
            ))
            .key("isBase64Encoded", bool());

    public static final StructureValidator HTTP_REQUEST_V1 = map()
            .key("version", fixed("1.0"))
            .key("resource", string())
            .key("path", string())
            .key("httpMethod", oneOf(
                    fixed("GET"),
                    fixed("POST"),
                    fixed("PUT"),
                    fixed("DELETE"),
                    fixed("OPTIONS")
            ))
            .key("headers", mapOf(string()))
            .key("multiValueHeaders", mapOf(listOf(string())))
            .key("queryStringParameters", oneOf(
                    nullValue(),
                    mapOf(string())
            ))
            .key("multiValueQueryStringParameters", oneOf(
                    nullValue(),
                    mapOf(listOf(string()))
            ))
            .key("requestContext", map()
                    .key("accountId", numeric())
                    .key("apiId", string())
                    .optionalKey("authorizer", map()
                            .key("claims", map()
                                    .key("auth_time", numeric())
                                    .key("client_id", string())
                                    .key("event_id", string())
                                    .key("exp", numeric())
                                    .key("iat", numeric())
                                    .key("iss", string())
                                    .key("jti", string())
                                    .key("scope", string())
                                    .key("sub", string())
                                    .key("token_use", string())
                                    .key("username", string())
                            )
                            .key("scopes", nullValue())
                    )
                    .key("domainName", string())
                    .key("domainPrefix", string())
                    .key("extendedRequestId", string())
                    .key("httpMethod", oneOf(
                            fixed("GET"),
                            fixed("POST"),
                            fixed("PUT"),
                            fixed("DELETE"),
                            fixed("OPTIONS")
                    ))
                    .key("identity", map()
                            .key("accessKey", nullValue())
                            .key("accountId", nullValue())
                            .key("caller", nullValue())
                            .key("cognitoAmr", nullValue())
                            .key("cognitoAuthenticationProvider", nullValue())
                            .key("cognitoAuthenticationType", nullValue())
                            .key("cognitoIdentityId", nullValue())
                            .key("cognitoIdentityPoolId", nullValue())
                            .key("principalOrgId", nullValue())
                            .key("sourceIp", string())
                            .key("user", nullValue())
                            .key("userAgent", fixed(""))
                            .key("userArn", nullValue())
                    )
                    .key("path", string())
                    .key("protocol", fixed("HTTP/1.1"))
                    .key("requestId", string())
                    .key("requestTime", string())
                    .key("requestTimeEpoch", number())
                    .key("resourceId", string())
                    .key("resourcePath", string())
                    .key("stage", string())
            )
            .key("pathParameters", nullValue())
            .key("stageVariables", nullValue())
            .key("body", oneOf(
                    nullValue(),
                    string()
            ))
            .key("isBase64Encoded", bool());

    public static final StructureValidator HTTP_REQUEST_V2 = map()
            .key("version", fixed("2.0"))
            .key("routeKey", string())
            .key("rawPath", string())
            .key("rawQueryString", string())
            .key("headers", mapOf(string()))
            .optionalKey("queryStringParameters", mapOf(string()))
            .optionalKey("cookies", listOf(string()))
            .key("requestContext", map()
                    .key("accountId", numeric())
                    .key("apiId", string())
                    .optionalKey("authorizer", map()
                            .key("jwt", map()
                                    .key("claims", map()
                                            .key("auth_time", numeric())
                                            .key("client_id", string())
                                            .key("event_id", string())
                                            .key("exp", numeric())
                                            .key("iat", numeric())
                                            .key("iss", string())
                                            .key("jti", string())
                                            .key("scope", string())
                                            .key("sub", string())
                                            .key("token_use", string())
                                            .key("username", string())
                                    )
                                    .key("scopes", nullValue())
                            )
                    )
                    .key("domainName", string())
                    .key("domainPrefix", string())
                    .key("http", map()
                            .key("method", oneOf(
                                    fixed("GET"),
                                    fixed("POST"),
                                    fixed("PUT"),
                                    fixed("DELETE"),
                                    fixed("OPTIONS")
                            ))
                            .key("path", string())
                            .key("protocol", fixed("HTTP/1.1"))
                            .key("sourceIp", string())
                            .key("userAgent", string())
                    )
                    .key("requestId", string())
                    .key("routeKey", string())
                    .key("stage", string())
                    .key("time", string())
                    .key("timeEpoch", number())
            )
            .optionalKey("body", string())
            .key("isBase64Encoded", bool());

    public static final StructureValidator WEBSOCKET_CONNECT = map()
            .key("headers", mapOf(string()))
            .key("multiValueHeaders", mapOf(listOf(string())))
            .optionalKey("queryStringParameters", mapOf(string()))
            .optionalKey("multiValueQueryStringParameters", mapOf(listOf(string())))
            .key("requestContext", map()
                    .key("routeKey", fixed("$connect"))
                    .optionalKey("disconnectStatusCode", nullValue())
                    .optionalKey("authorizer", map()
                            .key("principalId", string())
                            .key("integrationLatency", number())
                            .key("registryEntry", string())
                    )
                    .optionalKey("messageId", nullValue())
                    .key("eventType", fixed("CONNECT"))
                    .key("extendedRequestId", string())
                    .key("requestTime", string())
                    .key("messageDirection", fixed("IN"))
                    .optionalKey("disconnectReason", nullValue())
                    .key("stage", string())
                    .key("connectedAt", number())
                    .key("requestTimeEpoch", number())
                    .key("identity", map()
                            .optionalKey("cognitoIdentityPoolId", nullValue())
                            .optionalKey("cognitoIdentityId", nullValue())
                            .optionalKey("principalOrgId", nullValue())
                            .optionalKey("cognitoAuthenticationType", nullValue())
                            .optionalKey("userArn", nullValue())
                            .key("userAgent", string())
                            .optionalKey("accountId", nullValue())
                            .optionalKey("caller", nullValue())
                            .key("sourceIp", string())
                            .optionalKey("accessKey", nullValue())
                            .optionalKey("cognitoAuthenticationProvider", nullValue())
                            .optionalKey("user", nullValue())
                    )
                    .key("requestId", string())
                    .key("domainName", string())
                    .key("connectionId", string())
                    .key("apiId", string())
            )
            .key("isBase64Encoded", bool());

    public static final StructureValidator WEBSOCKET_MESSAGE = map()
            .key("requestContext", map()
                    .key("routeKey", fixed("$default"))
                    .optionalKey("disconnectStatusCode", nullValue())
                    .optionalKey("authorizer", map()
                            .key("principalId", string())
                            .key("registryEntry", string())
                    )
                    .key("messageId", string())
                    .key("eventType", fixed("MESSAGE"))
                    .key("extendedRequestId", string())
                    .key("requestTime", string())
                    .key("messageDirection", fixed("IN"))
                    .optionalKey("disconnectReason", nullValue())
                    .key("stage", string())
                    .key("connectedAt", number())
                    .key("requestTimeEpoch", number())
                    .key("identity", map()
                            .optionalKey("cognitoIdentityPoolId", nullValue())
                            .optionalKey("cognitoIdentityId", nullValue())
                            .optionalKey("principalOrgId", nullValue())
                            .optionalKey("cognitoAuthenticationType", nullValue())
                            .optionalKey("userArn", nullValue())
                            .key("userAgent", string())
                            .optionalKey("accountId", nullValue())
                            .optionalKey("caller", nullValue())
                            .key("sourceIp", string())
                            .optionalKey("accessKey", nullValue())
                            .optionalKey("cognitoAuthenticationProvider", nullValue())
                            .optionalKey("user", nullValue())
                    )
                    .key("requestId", string())
                    .key("domainName", string())
                    .key("connectionId", string())
                    .key("apiId", string())
            )
            .key("body", string())
            .key("isBase64Encoded", bool());

    public static final StructureValidator WEBSOCKET_DISCONNECT = map()
            .key("headers", mapOf(string()))
            .key("multiValueHeaders", mapOf(listOf(string())))
            .key("requestContext", map()
                    .key("routeKey", fixed("$disconnect"))
                    .optionalKey("disconnectStatusCode", number())
                    .optionalKey("authorizer", map()
                            .key("principalId", string())
                            .key("registryEntry", string())
                    )
                    .optionalKey("messageId", nullValue())
                    .key("eventType", fixed("DISCONNECT"))
                    .key("extendedRequestId", string())
                    .key("requestTime", string())
                    .key("messageDirection", fixed("IN"))
                    .optionalKey("disconnectReason", oneOf(
                            fixed("Shutdown"),
                            fixed("Connection Closed Normally")
                    ))
                    .key("stage", string())
                    .key("connectedAt", number())
                    .key("requestTimeEpoch", number())
                    .key("identity", map()
                            .optionalKey("cognitoIdentityPoolId", nullValue())
                            .optionalKey("cognitoIdentityId", nullValue())
                            .optionalKey("principalOrgId", nullValue())
                            .optionalKey("cognitoAuthenticationType", nullValue())
                            .optionalKey("userArn", nullValue())
                            .key("userAgent", string())
                            .optionalKey("accountId", nullValue())
                            .optionalKey("caller", nullValue())
                            .key("sourceIp", string())
                            .optionalKey("accessKey", nullValue())
                            .optionalKey("cognitoAuthenticationProvider", nullValue())
                            .optionalKey("user", nullValue())
                    )
                    .key("requestId", string())
                    .key("domainName", string())
                    .key("connectionId", string())
                    .key("apiId", string())
            )
            .key("isBase64Encoded", bool());

    public static final StructureValidator WEBSOCKET_AUTHORIZATION = map()
            .key("type", fixed("REQUEST"))
            .key("methodArn", string())
            .key("headers", mapOf(string()))
            .key("multiValueHeaders", mapOf(listOf(string())))
            .key("queryStringParameters", mapOf(string()))
            .key("multiValueQueryStringParameters", mapOf(listOf(string())))
            .key("stageVariables", emptyMap())
            .key("requestContext", map()
                    .key("routeKey", fixed("$connect"))
                    .optionalKey("disconnectStatusCode", nullValue())
                    .optionalKey("messageId", nullValue())
                    .key("eventType", fixed("CONNECT"))
                    .key("extendedRequestId", string())
                    .key("requestTime", string())
                    .key("messageDirection", fixed("IN"))
                    .optionalKey("disconnectReason", nullValue())
                    .key("stage", string())
                    .key("connectedAt", number())
                    .key("requestTimeEpoch", number())
                    .key("identity", map()
                            .optionalKey("cognitoIdentityPoolId", nullValue())
                            .optionalKey("cognitoIdentityId", nullValue())
                            .optionalKey("principalOrgId", nullValue())
                            .optionalKey("cognitoAuthenticationType", nullValue())
                            .optionalKey("userArn", nullValue())
                            .key("userAgent", string())
                            .optionalKey("accountId", nullValue())
                            .optionalKey("caller", nullValue())
                            .key("sourceIp", string())
                            .optionalKey("accessKey", nullValue())
                            .key("cognitoAuthenticationProvider", nullValue())
                            .optionalKey("user", nullValue())
                    )
                    .key("requestId", string())
                    .key("domainName", string())
                    .key("connectionId", string())
                    .key("apiId", string())
            );

    public static final StructureValidator LAMBDA_EVENT = oneOf(
            REST_REQUEST,
            HTTP_REQUEST_V1,
            HTTP_REQUEST_V2,
            WEBSOCKET_CONNECT,
            WEBSOCKET_MESSAGE,
            WEBSOCKET_DISCONNECT,
            WEBSOCKET_AUTHORIZATION
    );

    private Structures() {
    }
}
