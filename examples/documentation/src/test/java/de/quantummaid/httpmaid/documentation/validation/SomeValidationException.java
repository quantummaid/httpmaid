package de.quantummaid.httpmaid.documentation.validation;

public final class SomeValidationException extends RuntimeException {

    private SomeValidationException() {
    }

    public static SomeValidationException someValidationException() {
        return new SomeValidationException();
    }
}
