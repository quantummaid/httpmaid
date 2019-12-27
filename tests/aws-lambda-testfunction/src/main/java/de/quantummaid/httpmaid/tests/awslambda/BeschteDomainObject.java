package de.quantummaid.httpmaid.tests.awslambda;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;


@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public class BeschteDomainObject {
    private final String value;

    public static BeschteDomainObject fromStringValue(final String input) {
        return new BeschteDomainObject(input);
    }

    public String stringValue() {
        return value;
    }
}
