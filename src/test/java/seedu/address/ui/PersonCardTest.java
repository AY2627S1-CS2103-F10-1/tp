package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    private static final Person PERSON = new PersonBuilder().withRemark("Call after 6pm").build();

    @BeforeAll
    public static void setUpJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // JavaFX has already been initialized by another test.
        }
    }

    @Test
    public void constructor_displaysRemark() throws InterruptedException {
        AtomicReference<String> displayedRemark = new AtomicReference<>();

        runOnJavaFxThread(() -> {
            PersonCard personCard = new PersonCard(PERSON, 1);
            Label remark = (Label) personCard.getRoot().lookup("#remark");
            displayedRemark.set(remark.getText());
        });

        assertEquals(PERSON.getRemark().value, displayedRemark.get());
    }

    private static void runOnJavaFxThread(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                thrown.set(throwable);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        if (thrown.get() != null) {
            throw new AssertionError(thrown.get());
        }
    }
}
