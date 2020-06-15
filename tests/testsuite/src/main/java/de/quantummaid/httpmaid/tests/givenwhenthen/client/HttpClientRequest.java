package de.quantummaid.httpmaid.tests.givenwhenthen.client;

import de.quantummaid.httpmaid.tests.givenwhenthen.Headers;
import de.quantummaid.httpmaid.tests.givenwhenthen.QueryStringParameter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpClientRequest {
    public final String path;
    public final String method;
    public final List<QueryStringParameter> queryStringParameters;
    public final Headers headers;

    public static HttpClientRequest httpClientRequest(final String path,
                                                      final String method,
                                                      final List<QueryStringParameter> queryStringParameters,
                                                      final Headers headers) {
        return new HttpClientRequest(path, method, List.copyOf(queryStringParameters), headers);
    }
}
