package de.quantummaid.httpmaid.websockets.registry;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.headers.ContentType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_CONTENT_TYPE;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_HEADERS;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_CONNECTION_INFORMATION;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketRegistryEntry {
    private final Object connectionInformation;
    private final Headers headers;
    private final ContentType contentType;

    private static WebsocketRegistryEntry websocketRegistryEntry(final Object connectionInformation,
                                                                 final Headers headers,
                                                                 final ContentType contentType) {
        validateNotNull(connectionInformation, "connectionInformation");
        validateNotNull(headers, "headers");
        validateNotNull(contentType, "contentType");
        return new WebsocketRegistryEntry(connectionInformation, headers, contentType);
    }

    public static WebsocketRegistryEntry loadFromMetaData(final MetaData metaData) {
        final Object connectionInformation = metaData.get(WEBSOCKET_CONNECTION_INFORMATION);
        final Headers headers = metaData.get(REQUEST_HEADERS);
        final ContentType contentType = metaData.get(REQUEST_CONTENT_TYPE);
        return websocketRegistryEntry(connectionInformation, headers, contentType);
    }

    public Object connectionInformation() {
        return connectionInformation;
    }

    public Headers headers() {
        return headers;
    }

    public ContentType contentType() {
        return contentType;
    }

    public void restoreToMetaData(final MetaData metaData) {
        metaData.set(REQUEST_HEADERS, headers);
        metaData.set(REQUEST_CONTENT_TYPE, contentType);
    }
}
