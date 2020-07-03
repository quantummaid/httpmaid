package de.quantummaid.httpmaid.websockets.criteria;

import de.quantummaid.httpmaid.http.HeaderName;
import de.quantummaid.httpmaid.http.HeaderValue;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderCriterion {
    private final HeaderName name;
    private final HeaderValue value;

    public static HeaderCriterion headerCriterion(final HeaderName name,
                                                  final HeaderValue value) {
        return new HeaderCriterion(name, value);
    }

    public boolean filter(final WebsocketRegistryEntry entry) {
        final List<String> values = entry.headers()
                .allValuesFor(name.stringValue());
        return values.contains(value.stringValue());
    }
}
