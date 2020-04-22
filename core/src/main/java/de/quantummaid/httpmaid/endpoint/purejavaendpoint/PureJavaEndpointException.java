package de.quantummaid.httpmaid.endpoint.purejavaendpoint;

public final class PureJavaEndpointException extends RuntimeException {

    private PureJavaEndpointException(final Throwable cause) {
        super(cause);
    }
    
    public static PureJavaEndpointException pureJavaEndpointException(final Throwable cause) {
        return new PureJavaEndpointException(cause);
    }
}
