package de.quantummaid.httpmaid.documentation.support.curl;

import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ResourceLoader {

    private ResourceLoader() {
    }

    public static String resourceToString(final String path) {
        final ClassLoader classLoader = Curl.class.getClassLoader();
        final URL url = classLoader.getResource(path);
        try {
            return Resources.toString(url, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
