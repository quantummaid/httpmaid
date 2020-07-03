package de.quantummaid.httpmaid.websockets.criteria;

import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static de.quantummaid.httpmaid.http.HeaderName.headerName;
import static de.quantummaid.httpmaid.http.HeaderValue.headerValue;
import static de.quantummaid.httpmaid.websockets.criteria.HeaderCriterion.headerCriterion;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketCriteria {
    private final List<HeaderCriterion> headerCriteria;

    public static WebsocketCriteria websocketCriteria() {
        return new WebsocketCriteria(new ArrayList<>());
    }

    public WebsocketCriteria header(final String name, final String value) {
        final HeaderCriterion headerCriterion = headerCriterion(headerName(name), headerValue(value));
        headerCriteria.add(headerCriterion);
        return this;
    }

    public boolean filter(final WebsocketRegistryEntry entry) {
        return headerCriteria.stream()
                .allMatch(headerCriterion -> headerCriterion.filter(entry));
    }
}
