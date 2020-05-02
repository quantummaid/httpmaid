package de.quantummaid.httpmaid.websockets;

public interface WebsocketSender<T> {
    void send(T connectionInformation, String message);
}
