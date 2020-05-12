package de.quantummaid.httpmaid.tests.givenwhenthen;

import java.util.function.Supplier;

import static java.lang.Thread.currentThread;

public final class Poller {
    private static final int WAIT_TIME = 120;
    private static final int SLEEP_TIME = 250;

    private Poller() {
    }

    public static boolean pollWithTimeout(final Supplier<Boolean> condition) {
        for (int i = 0; i < WAIT_TIME; ++i) {
            final Boolean conditionHasBeenFullfilled = condition.get();
            if (conditionHasBeenFullfilled) {
                return true;
            }
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (final InterruptedException e) {
                currentThread().interrupt();
            }
        }
        return false;
    }
}
