package de.quantummaid.httpmaid.documentation.validation;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.documentation.validation.SomeValidationException.someValidationException;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlwaysInvalidType {
    public final String field1;
    public final String field2;
    public final String field3;

    public static AlwaysInvalidType alwaysInvalidType(final String field1,
                                                      final String field2,
                                                      final String field3) {
        throw someValidationException();
    }
}
