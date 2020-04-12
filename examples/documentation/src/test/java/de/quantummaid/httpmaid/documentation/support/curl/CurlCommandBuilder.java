package de.quantummaid.httpmaid.documentation.support.curl;

import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.client.HttpClientRequest.aRequest;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CurlCommandBuilder {
    private String path;
    private String method = null;
    private String body = null;
    private final Map<String, String> headers = new HashMap<>();

    public static CurlCommandBuilder curlCommandBuilder() {
        return new CurlCommandBuilder();
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public void setBody(final String body) {
        this.body = body;
        if (method == null) {
            setMethod("POST");
        }
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void addHeader(final String key, final String value) {
        headers.put(key, value);
    }

    public HttpClientRequestBuilder<SimpleHttpResponseObject> build() {
        if (method == null) {
            method = "GET";
        }
        final HttpClientRequestBuilder<SimpleHttpResponseObject> builder = aRequest(method, path);
        headers.forEach(builder::withHeader);
        if (body != null) {
            builder.withTheBody(body);
        }
        return builder;
    }
}
