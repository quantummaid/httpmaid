/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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
import de.quantummaid.httpmaid.closing.ClosingAction;
import de.quantummaid.httpmaid.closing.ClosingActions;
import de.quantummaid.httpmaid.exceptions.ExceptionMapper;
import de.quantummaid.httpmaid.exceptions.ExceptionSerializer;
import de.quantummaid.httpmaid.filtermap.FilterMapBuilder;
import de.quantummaid.httpmaid.generator.GenerationCondition;
import de.quantummaid.httpmaid.generator.Generator;
import de.quantummaid.httpmaid.handler.Handler;
import de.quantummaid.httpmaid.handler.distribution.HandlerDistributors;
import de.quantummaid.httpmaid.logger.LoggerImplementation;
import de.quantummaid.httpmaid.responsetemplate.ResponseTemplate;
import de.quantummaid.httpmaid.backchannel.BackChannelFactory;
import de.quantummaid.httpmaid.backchannel.LocalBackChannelFactory;
import de.quantummaid.httpmaid.chains.builder.ChainBuilder;
import de.quantummaid.httpmaid.chains.rules.Consume;
import de.quantummaid.httpmaid.chains.rules.Jump;
import de.quantummaid.httpmaid.exceptions.DefaultExceptionMapper;
import de.quantummaid.httpmaid.generator.Generators;
import de.quantummaid.httpmaid.handler.DetermineHandlerProcessor;
import de.quantummaid.httpmaid.handler.InvokeHandlerProcessor;
import de.quantummaid.httpmaid.http.Http;
import de.quantummaid.httpmaid.logger.Loggers;
import de.quantummaid.httpmaid.logger.SetLoggerProcessor;
import de.quantummaid.httpmaid.processors.MapExceptionProcessor;
import de.quantummaid.httpmaid.responsetemplate.ApplyResponseTemplateProcessor;
import de.quantummaid.httpmaid.responsetemplate.InitResponseProcessor;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static de.quantummaid.httpmaid.HttpMaidChains.*;
import static de.quantummaid.httpmaid.handler.distribution.HandlerDistributors.HANDLER_DISTRIBUTORS;
import static de.quantummaid.httpmaid.handler.distribution.HandlerDistributors.handlerDistributors;
import static de.quantummaid.httpmaid.processors.StreamToStringProcessor.streamToStringProcessor;
import static de.quantummaid.httpmaid.processors.StringBodyToStreamProcessor.stringBodyToStreamProcessor;
import static de.quantummaid.httpmaid.processors.TranslateToValueObjectsProcessor.translateToValueObjectsProcessor;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoreModule implements ChainModule {
    private final Map<GenerationCondition, Object> handlers = new HashMap<>();
    private final List<Generator<Handler>> lowLevelHandlers = new LinkedList<>();
    private ResponseTemplate responseTemplate = ResponseTemplate.EMPTY_RESPONSE_TEMPLATE;
    private final FilterMapBuilder<Throwable, ExceptionMapper<Throwable>> exceptionMappers = FilterMapBuilder.filterMapBuilder();
    private LoggerImplementation logger = Loggers.stdoutAndStderrLogger();
    private final ClosingActions closingActions = ClosingActions.closingActions();

    public static CoreModule coreModule() {
        final CoreModule coreModule = new CoreModule();
        coreModule.setDefaultExceptionMapper(DefaultExceptionMapper.theDefaultExceptionMapper());
        return coreModule;
    }

    public void registerHandler(final GenerationCondition condition,
                                final Object handler) {
        Validators.validateNotNull(condition, "generationCondition");
        Validators.validateNotNull(handler, "handler");
        handlers.put(condition, handler);
    }

    public void setLogger(final LoggerImplementation logger) {
        Validators.validateNotNull(logger, "logger");
        this.logger = logger;
    }

    public void addClosingAction(final ClosingAction closingAction) {
        Validators.validateNotNull(closingAction, "closingAction");
        closingActions.addClosingAction(closingAction);
    }

    public void setResponseTemplate(final ResponseTemplate responseTemplate) {
        Validators.validateNotNull(responseTemplate, "responseTemplate");
        this.responseTemplate = responseTemplate;
    }

    public void addExceptionMapper(final Predicate<Throwable> filter,
                                   final ExceptionMapper<Throwable> responseMapper) {
        Validators.validateNotNull(filter, "filter");
        Validators.validateNotNull(responseMapper, "responseMapper");
        this.exceptionMappers.put(filter, responseMapper);
    }

    public void setDefaultExceptionMapper(final ExceptionMapper<Throwable> responseMapper) {
        Validators.validateNotNull(responseMapper, "responseMapper");
        this.exceptionMappers.setDefaultValue(responseMapper);
    }

    @Override
    public void init(final MetaData configurationMetaData) {
        final HandlerDistributors handlerDistributers = handlerDistributors();
        configurationMetaData.set(HANDLER_DISTRIBUTORS, handlerDistributers);
        handlerDistributers.register(handler -> handler instanceof Handler, (handler, condition) -> {
            final Generator<Handler> generator = Generator.generator((Handler) handler, condition);
            lowLevelHandlers.add(generator);
        });
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final HandlerDistributors handlerDistributors = dependencyRegistry.getMetaDatum(HANDLER_DISTRIBUTORS);
        handlers.forEach((condition, handler) -> handlerDistributors.distribute(handler, condition));
    }

    @Override
    public void register(final ChainExtender extender) {
        final ExceptionSerializer exceptionSerializer = ExceptionSerializer.exceptionSerializer(exceptionMappers.build());
        ChainBuilder.extendAChainWith(extender)
                .append(INIT, SetLoggerProcessor.setLoggerProcessor(logger))
                .append(PRE_PROCESS, translateToValueObjectsProcessor())
                .append(PROCESS_HEADERS)
                .append(PROCESS_BODY)
                .append(PROCESS_BODY_STRING, streamToStringProcessor())
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
        extender.addMetaDatum(BackChannelFactory.BACK_CHANNEL_FACTORY, LocalBackChannelFactory.localBackChannelFactory());
    }
}
