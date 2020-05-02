package de.quantummaid.httpmaid.websockets;

import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.ChainName;
import de.quantummaid.httpmaid.chains.builder.ChainBuilder;
import de.quantummaid.httpmaid.chains.rules.Jump;
import de.quantummaid.httpmaid.generator.Generators;
import de.quantummaid.httpmaid.handler.DetermineHandlerProcessor;
import de.quantummaid.httpmaid.handler.InvokeHandlerProcessor;
import de.quantummaid.httpmaid.responsetemplate.ApplyResponseTemplateProcessor;
import de.quantummaid.httpmaid.responsetemplate.InitResponseProcessor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.HttpMaidChains.*;
import static de.quantummaid.httpmaid.chains.ChainName.chainName;
import static de.quantummaid.httpmaid.processors.StreamToStringProcessor.streamToStringProcessor;
import static de.quantummaid.httpmaid.processors.TranslateToValueObjectsProcessor.translateToValueObjectsProcessor;
import static de.quantummaid.httpmaid.websockets.processor.DetermineWebsocketCategoryProcessor.determineWebsocketCategoryProcessor;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketsModule implements ChainModule {
    private static final ChainName SEND_WEBSOCKET_MESSAGE = chainName("SEND_WEBSOCKET_MESSAGE");

    private String categoryKey = "message";

    public static WebsocketsModule websocketsModule() {
        return new WebsocketsModule();
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.appendProcessor(PRE_DETERMINE_HANDLER, determineWebsocketCategoryProcessor(categoryKey));


        ChainBuilder.extendAChainWith(extender)
                .append(SEND_WEBSOCKET_MESSAGE);
    }
}
