package de.quantummaid.httpmaid.websockets.sender;

public interface NonSerializableConnectionInformation {
    void send(String message);
}
