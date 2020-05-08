package de.quantummaid.httpmaid.websockets;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.generator.GenerationCondition;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketCategory implements GenerationCondition {
    private final String category;

    public static GenerationCondition webSocketCategory(final String category) {
        validateNotNull(category, "category");
        return new WebsocketCategory(category);
    }

    @Override
    public boolean generate(final MetaData metaData) {
        final Boolean websocket = metaData.getOptional(REQUEST_TYPE)
                .map(WEBSOCKET_MESSAGE::equals)
                .orElse(false);
        if (!websocket) {
            return false;
        }
        return metaData.getOptional(WEBSOCKET_CATEGORY)
                .map(category::equals)
                .orElse(false);
    }
}
