package de.quantummaid.httpmaid.documentation.support.browser;

public interface Browser {
    void request(String route);

    void injectCookie(String key, String value);

    void assertCurrentPageContains(String content);

    void assertCookieExists(String cookie);
}
