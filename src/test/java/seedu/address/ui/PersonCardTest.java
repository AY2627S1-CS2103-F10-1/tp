package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {
    @BeforeAll
    public static void initialiseJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException e) {
            // Toolkit was already started by another test.
        }
    }

    @Test
    public void constructor_storesPerson() {
        var person = new PersonBuilder().withRemark("test remark").build();
        PersonCard card = new PersonCard(person, 1);
        assertSame(person, card.person);
    }
}
