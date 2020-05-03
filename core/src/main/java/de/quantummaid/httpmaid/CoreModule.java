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

package de.quantummaid.httpmaid;

import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.DependencyRegistry;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.builder.ChainBuilder;
import de.quantummaid.httpmaid.chains.rules.Consume;
import de.quantummaid.httpmaid.chains.rules.Jump;
import de.quantummaid.httpmaid.closing.ClosingActions;
import de.quantummaid.httpmaid.exceptions.ExceptionMapper;
import de.quantummaid.httpmaid.exceptions.ExceptionSerializer;
import de.quantummaid.httpmaid.filtermap.FilterMapBuilder;
import de.quantummaid.httpmaid.generator.GenerationCondition;
import de.quantummaid.httpmaid.generator.Generator;
import de.quantummaid.httpmaid.generator.Generators;
import de.quantummaid.httpmaid.handler.DetermineHandlerProcessor;
import de.quantummaid.httpmaid.handler.Handler;
import de.quantummaid.httpmaid.handler.InvokeHandlerProcessor;
import de.quantummaid.httpmaid.handler.distribution.DistributableHandler;
import de.quantummaid.httpmaid.handler.distribution.HandlerDistributors;
import de.quantummaid.httpmaid.http.Http;
import de.quantummaid.httpmaid.processors.MapExceptionProcessor;
import de.quantummaid.httpmaid.responsetemplate.ApplyResponseTemplateProcessor;
import de.quantummaid.httpmaid.responsetemplate.InitResponseProcessor;
import de.quantummaid.httpmaid.responsetemplate.ResponseTemplate;
import de.quantummaid.httpmaid.startupchecks.StartupChecks;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static de.quantummaid.httpmaid.HttpMaidChains.*;
import static de.quantummaid.httpmaid.exceptions.DefaultExceptionMapper.theDefaultExceptionMapper;
import static de.quantummaid.httpmaid.handler.distribution.DistributableHandler.distributableHandler;
import static de.quantummaid.httpmaid.handler.distribution.HandlerDistributors.HANDLER_DISTRIBUTORS;
import static de.quantummaid.httpmaid.handler.distribution.HandlerDistributors.handlerDistributors;
import static de.quantummaid.httpmaid.processors.StreamToStringProcessor.streamToStringProcessor;
import static de.quantummaid.httpmaid.processors.StringBodyToStreamProcessor.stringBodyToStreamProcessor;
import static de.quantummaid.httpmaid.processors.TranslateToValueObjectsProcessor.translateToValueObjectsProcessor;
import static de.quantummaid.httpmaid.responsetemplate.ResponseTemplate.emptyResponseTemplate;
import static de.quantummaid.httpmaid.startupchecks.StartupChecks.STARTUP_CHECKS;
import static de.quantummaid.httpmaid.startupchecks.StartupChecks.startupChecks;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Collections.emptyList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoreModule implements ChainModule {
    private final List<DistributableHandler> handlers = new ArrayList<>();
    private final List<Generator<Handler>> lowLevelHandlers = new LinkedList<>();
    private ResponseTemplate responseTemplate = emptyResponseTemplate();
    private final FilterMapBuilder<Throwable, ExceptionMapper<Throwable>> exceptionMappers = FilterMapBuilder.filterMapBuilder();
    private final ClosingActions closingActions = ClosingActions.closingActions();

    public static CoreModule coreModule() {
        final CoreModule coreModule = new CoreModule();
        coreModule.setDefaultExceptionMapper(theDefaultExceptionMapper());
        return coreModule;
    }

    public void registerHandler(final GenerationCondition condition,
                                final Object handler,
                                final List<PerRouteConfigurator> perRouteConfigurators) {
        validateNotNull(condition, "generationCondition");
        validateNotNull(handler, "handler");
        validateNotNull(perRouteConfigurators, "perRouteConfigurators");
        handlers.add(distributableHandler(condition, handler, perRouteConfigurators));
    }

    public void setResponseTemplate(final ResponseTemplate responseTemplate) {
        validateNotNull(responseTemplate, "responseTemplate");
        this.responseTemplate = responseTemplate;
    }

    public void addExceptionMapper(final Predicate<Throwable> filter,
                                   final ExceptionMapper<Throwable> responseMapper) {
        validateNotNull(filter, "filter");
        validateNotNull(responseMapper, "responseMapper");
        this.exceptionMappers.put(filter, responseMapper);
    }

    public void setDefaultExceptionMapper(final ExceptionMapper<Throwable> responseMapper) {
        validateNotNull(responseMapper, "responseMapper");
        this.exceptionMappers.setDefaultValue(responseMapper);
    }

    @Override
    public void init(final MetaData configurationMetaData) {
        final StartupChecks startupChecks = startupChecks();
        configurationMetaData.set(STARTUP_CHECKS, startupChecks);
        final HandlerDistributors handlerDistributers = handlerDistributors();
        configurationMetaData.set(HANDLER_DISTRIBUTORS, handlerDistributers);
        handlerDistributers.register(handler -> handler.handler() instanceof Handler, handler -> {
            final Generator<Handler> generator = Generator.generator((Handler) handler.handler(), handler.condition());
            lowLevelHandlers.add(generator);
            return emptyList();
        });
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final HandlerDistributors handlerDistributors = dependencyRegistry.getMetaDatum(HANDLER_DISTRIBUTORS);
        handlers.forEach(handler -> handlerDistributors.distribute(handler, dependencyRegistry));
    }

    @Override
    public void register(final ChainExtender extender) {
        final ExceptionSerializer exceptionSerializer = ExceptionSerializer.exceptionSerializer(exceptionMappers.build());
        ChainBuilder.extendAChainWith(extender)
                .append(INIT)
                .append(PRE_PROCESS, translateToValueObjectsProcessor())
                .append(PROCESS_HEADERS)
                .append(PROCESS_BODY)
                .append(PROCESS_BODY_STRING, streamToStringProcessor())
                .append(PRE_DETERMINE_HANDLER)
                .append(DETERMINE_HANDLER, DetermineHandlerProcessor.determineHandlerProcessor(Generators.generators(lowLevelHandlers)))
                .append(PREPARE_RESPONSE, InitResponseProcessor.initResponseProcessor(), ApplyResponseTemplateProcessor.applyResponseTemplateProcessor(responseTemplate))
                .append(INVOKE_HANDLER, InvokeHandlerProcessor.invokeHandlerProcessor())
                .append(POST_INVOKE)
                .withTheExceptionChain(EXCEPTION_OCCURRED)
                .withTheFinalAction(Jump.jumpTo(POST_PROCESS));

        ChainBuilder.extendAChainWith(extender)
                .append(EXCEPTION_OCCURRED)
                .append(PREPARE_EXCEPTION_RESPONSE,
                        InitResponseProcessor.initResponseProcessor(),
                        metaData -> metaData.set(HttpMaidChainKeys.RESPONSE_STATUS, Http.StatusCodes.INTERNAL_SERVER_ERROR))
                .append(MAP_EXCEPTION_TO_RESPONSE, MapExceptionProcessor.mapExceptionProcessor(exceptionSerializer))
                .withTheExceptionChain(ERROR)
                .withTheFinalAction(Jump.jumpTo(POST_INVOKE));

        extender.createChain(POST_PROCESS, Consume.consume(), Jump.jumpTo(ERROR));
        extender.appendProcessor(POST_PROCESS, stringBodyToStreamProcessor());

        extender.createChain(ERROR, Consume.consume(), Consume.consume());

        extender.addMetaDatum(ClosingActions.CLOSING_ACTIONS, closingActions);
    }
}
