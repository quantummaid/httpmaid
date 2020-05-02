package de.quantummaid.httpmaid.websockets.registry;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectionId {
    private final String id;

    public static ConnectionId connectionId(final String id) {
        validateNotNull(id, "id");
        return new ConnectionId(id);
    }
}
