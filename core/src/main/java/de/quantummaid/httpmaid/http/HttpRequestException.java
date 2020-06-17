package de.quantummaid.httpmaid.http;

public final class HttpRequestException extends RuntimeException {

    private HttpRequestException(final String message) {
        super(message);
    }

    public static HttpRequestException httpHandlerException(final String message) {
        return new HttpRequestException(message);
    }
}
