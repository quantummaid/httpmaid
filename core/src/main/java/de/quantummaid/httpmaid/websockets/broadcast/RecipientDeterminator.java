package de.quantummaid.httpmaid.websockets.broadcast;

public interface RecipientDeterminator {
    static RecipientDeterminator all() {
        return () -> true;
    }

    boolean isRecipient();
}
