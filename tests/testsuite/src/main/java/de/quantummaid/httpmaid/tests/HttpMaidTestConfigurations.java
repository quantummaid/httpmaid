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

package de.quantummaid.httpmaid.tests;

import com.google.gson.Gson;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.handler.NoHandlerFoundException;
import de.quantummaid.httpmaid.tests.usecases.ToStringWrapper;
import de.quantummaid.httpmaid.tests.usecases.echobody.EchoBodyUseCase;
import de.quantummaid.httpmaid.tests.usecases.echocontenttype.EchoContentTypeUseCase;
import de.quantummaid.httpmaid.tests.usecases.echomultipart.EchoMultipartUseCase;
import de.quantummaid.httpmaid.tests.usecases.echopathandqueryparameters.EchoPathAndQueryParametersUseCase;
import de.quantummaid.httpmaid.tests.usecases.echopathandqueryparameters.EchoPathAndQueryParametersValue;
import de.quantummaid.httpmaid.tests.usecases.headers.HeaderUseCase;
import de.quantummaid.httpmaid.tests.usecases.headers.HeadersParameter;
import de.quantummaid.httpmaid.tests.usecases.mapmaid.MapMaidUseCase;
import de.quantummaid.httpmaid.tests.usecases.multipartandmapmaid.MultipartAndMapMaidUseCase;
import de.quantummaid.httpmaid.tests.usecases.parameter.Parameter;
import de.quantummaid.httpmaid.tests.usecases.parameter.ParameterizedUseCase;
import de.quantummaid.httpmaid.tests.usecases.pathparameter.WildCardUseCase;
import de.quantummaid.httpmaid.tests.usecases.pathparameter.WildcardParameter;
import de.quantummaid.httpmaid.tests.usecases.queryparameters.QueryParametersParameter;
import de.quantummaid.httpmaid.tests.usecases.queryparameters.QueryParametersUseCase;
import de.quantummaid.httpmaid.tests.usecases.responsecontenttype.SetContentTypeInResponseUseCase;
import de.quantummaid.httpmaid.tests.usecases.responsecontenttype.SetContentTypeInResponseValue;
import de.quantummaid.httpmaid.tests.usecases.responseheaders.HeadersInResponseReturnValue;
import de.quantummaid.httpmaid.tests.usecases.responseheaders.HeadersInResponseUseCase;
import de.quantummaid.httpmaid.tests.usecases.simple.TestUseCase;
import de.quantummaid.httpmaid.tests.usecases.twoparameters.Parameter1;
import de.quantummaid.httpmaid.tests.usecases.twoparameters.Parameter2;
import de.quantummaid.httpmaid.tests.usecases.twoparameters.TwoParametersUseCase;
import de.quantummaid.httpmaid.tests.usecases.vooooid.VoidUseCase;
import de.quantummaid.httpmaid.usecases.UseCasesModule;

import java.util.Map;
import java.util.Objects;

import static de.quantummaid.httpmaid.Configurators.toCustomizeResponsesUsing;
import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_HEADERS;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_STATUS;
import static de.quantummaid.httpmaid.chains.Configurator.configuratorForType;
import static de.quantummaid.httpmaid.events.EventConfigurators.toEnrichTheIntermediateMapWithAllRequestData;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static de.quantummaid.httpmaid.http.Http.Headers.CONTENT_TYPE;
import static de.quantummaid.httpmaid.http.Http.StatusCodes.METHOD_NOT_ALLOWED;
import static de.quantummaid.httpmaid.http.Http.StatusCodes.OK;
import static de.quantummaid.httpmaid.http.HttpRequestMethod.*;
import static de.quantummaid.httpmaid.http.headers.ContentType.json;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toMarshallContentType;
import static java.util.Map.of;

public final class HttpMaidTestConfigurations {

    private HttpMaidTestConfigurations() {
    }

    @SuppressWarnings("unchecked")
    public static HttpMaid theHttpMaidInstanceUsedForTesting() {
        return anHttpMaid()
                .serving(TestUseCase.class).forRequestPath("/test").andRequestMethods(GET, POST, PUT, DELETE)
                .serving(EchoBodyUseCase.class).forRequestPath("/echo_body").andRequestMethods(GET, POST, PUT, DELETE)
                .get("/wild/<parameter>/card", WildCardUseCase.class)
                .get("/parameterized", ParameterizedUseCase.class)
                .get("/queryparameters", QueryParametersUseCase.class)
                .get("/headers", HeaderUseCase.class)
                .get("/headers_response", HeadersInResponseUseCase.class)
                .get("/echo_contenttype", EchoContentTypeUseCase.class)
                .get("/set_contenttype_in_response", SetContentTypeInResponseUseCase.class)
                .serving(EchoMultipartUseCase.class).forRequestPath("/multipart_echo").andRequestMethods(GET, POST, PUT, DELETE)
                .serving(MapMaidUseCase.class).forRequestPath("/mapmaid/<value1>").andRequestMethods(GET, POST)
                .get("/echo_path_and_query_parameters/<wildcard>", EchoPathAndQueryParametersUseCase.class)
                .get("/twoparameters", TwoParametersUseCase.class)
                .get("/void", VoidUseCase.class)
                .put("/multipart_and_mapmaid", MultipartAndMapMaidUseCase.class)

                .configured(toMarshallContentType(json(),
                        string -> new Gson().fromJson(string, Map.class),
                        map -> new Gson().toJson(map)))
                .configured(configuratorForType(UseCasesModule.class, useCasesModule -> {
                    useCasesModule.addRequestMapperForType(Parameter.class, (targetType, map) -> new Parameter());
                    useCasesModule.addRequestMapperForType(WildcardParameter.class, (targetType, map) -> new WildcardParameter((String) map.get("parameter")));
                    useCasesModule.addRequestMapperForType(QueryParametersParameter.class, (targetType, map) -> new QueryParametersParameter((Map<String, String>) (Object) map));
                    useCasesModule.addRequestMapperForType(HeadersParameter.class, (targetType, map) -> new HeadersParameter((Map<String, String>) (Object) map));
                    useCasesModule.addRequestMapperForType(EchoPathAndQueryParametersValue.class, (targetType, map) -> new EchoPathAndQueryParametersValue((Map<String, String>) (Object) map));
                    useCasesModule.addRequestMapperForType(Parameter1.class, (targetType, map) -> {
                        final Object param1 = map.get("param1");
                        return new Parameter1((String) param1);
                    });
                    useCasesModule.addRequestMapperForType(Parameter2.class, (targetType, map) -> {
                        final Object param2 = map.get("param2");
                        return new Parameter2((String) param2);
                    });

                    useCasesModule.addResponseSerializerForType(HeadersInResponseReturnValue.class, value -> of(value.key, value.value));
                    useCasesModule.addResponseSerializerForType(SetContentTypeInResponseValue.class, value -> of("contentType", value.value));
                    useCasesModule.addResponseSerializer(Objects::isNull, object -> null);
                    useCasesModule.addResponseSerializerForType(String.class, string -> of("response", string));
                    useCasesModule.addResponseSerializerForType(ToStringWrapper.class, wrapper -> of("response", wrapper.toString()));
                }))
                .configured(toEnrichTheIntermediateMapWithAllRequestData())

                .configured(toMapExceptionsOfType(NoHandlerFoundException.class, (exception, response) -> {
                    response.setStatus(METHOD_NOT_ALLOWED);
                    response.setBody("No use case found.");
                }))
                .configured(toCustomizeResponsesUsing(metaData -> {
                    metaData.set(RESPONSE_STATUS, OK);
                    metaData.get(RESPONSE_HEADERS).put(CONTENT_TYPE, "application/json");
                }))
                .build();
    }
}
