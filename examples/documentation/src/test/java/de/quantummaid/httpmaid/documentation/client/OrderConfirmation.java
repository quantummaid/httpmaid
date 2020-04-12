package de.quantummaid.httpmaid.documentation.client;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderConfirmation {
    private final String id;

    public static OrderConfirmation orderConfirmation(final String id) {
        return new OrderConfirmation(id);
    }

    public String getId() {
        return id;
    }
}
