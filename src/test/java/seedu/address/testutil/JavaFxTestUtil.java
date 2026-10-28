package seedu.address.testutil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/** Shares the native JavaFX toolkit across UI tests in the same test worker. */
public final class JavaFxTestUtil {
    private static final CompletableFuture<Void> ready = new CompletableFuture<>();
    private static boolean isStarted;

    private JavaFxTestUtil() {
    }

    /** Initializes JavaFX once and keeps it alive between test classes. */
    public static synchronized void initializeToolkit() throws Exception {
        if (!isStarted) {
            isStarted = true;
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                ready.complete(null);
            });
        }
        ready.get(10, TimeUnit.SECONDS);
    }
}
