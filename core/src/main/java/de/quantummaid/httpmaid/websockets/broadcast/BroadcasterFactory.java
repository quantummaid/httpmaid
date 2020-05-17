package de.quantummaid.httpmaid.websockets.broadcast;

public interface BroadcasterFactory<T, U> {
    T createBroadcaster(SerializingSender<U> sender);
}
