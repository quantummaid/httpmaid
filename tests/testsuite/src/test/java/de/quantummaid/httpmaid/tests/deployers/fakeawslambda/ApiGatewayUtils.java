package de.quantummaid.httpmaid.tests.deployers.fakeawslambda;

import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

import static de.quantummaid.httpmaid.util.streams.Streams.inputStreamToString;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class ApiGatewayUtils {

    private ApiGatewayUtils() {
    }

    public static void addBodyToEvent(final InputStream bodyStream, final Map<String, Object> event) {
        final String body = inputStreamToString(bodyStream);
        if (body.isEmpty()) {
            event.put("isBase64Encoded", false);
        } else {
            final String encodedBody = encodeBase64(body);
            event.put("body", encodedBody);
            event.put("isBase64Encoded", true);
        }
    }

    private static String encodeBase64(final String unencoded) {
        final Base64.Encoder encoder = Base64.getEncoder();
        final byte[] bytes = encoder.encode(unencoded.getBytes(UTF_8));
        return new String(bytes);
    }
}
