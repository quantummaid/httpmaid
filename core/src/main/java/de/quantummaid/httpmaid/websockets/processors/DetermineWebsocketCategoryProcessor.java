package de.quantummaid.httpmaid.websockets.processors;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.UNMARSHALLED_REQUEST_BODY;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_CATEGORY;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DetermineWebsocketCategoryProcessor implements Processor {
    private final String categoryKey;

    public static Processor determineWebsocketCategoryProcessor(final String categoryKey) {
        validateNotNull(categoryKey, "categoryKey");
        return new DetermineWebsocketCategoryProcessor(categoryKey);
    }

    @Override
    public void apply(final MetaData metaData) {
        metaData.getOptional(UNMARSHALLED_REQUEST_BODY).ifPresent(unmarshalledRequestBody -> {
            if (!(unmarshalledRequestBody instanceof Map)) {
                return;
            }
            final Map<String, Object> map = (Map<String, Object>) unmarshalledRequestBody;
            if (!map.containsKey(categoryKey)) {
                return;
            }
            final Object category = map.get(categoryKey);
            if (category instanceof String) {
                metaData.set(WEBSOCKET_CATEGORY, (String) category);
            }
        });
    }
}
