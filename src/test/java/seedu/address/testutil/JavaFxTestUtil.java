package seedu.address.testutil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/** Shares the native JavaFX toolkit across UI tests in the same test worker. */
public final class JavaFxTestUtil {
    private static final int TIMEOUT_SECONDS = 10;
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
        ready.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Runs assertions on the JavaFX application thread and propagates failures to the test thread.
     * The toolkit must have been initialized before this method is called.
     */
    public static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            assertions.run();
            return null;
        });
        Platform.runLater(task);
        task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
