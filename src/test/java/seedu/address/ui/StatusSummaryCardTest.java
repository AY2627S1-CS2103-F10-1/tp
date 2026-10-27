package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.text.Text;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.testutil.PersonBuilder;

public class StatusSummaryCardTest {
    @Test
    public void constructor_statusCard_setsTitleIdsAndStyle() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        StatusSummaryCard card = new StatusSummaryCard(Status.INTERVIEWING, persons, persons);

        assertEquals("Interviewing", ((Text) card.getRoot().lookup("#heading")).getText());
        assertEquals("interviewingSummaryCard", card.getRoot().getId());
        assertTrue(card.getRoot().getStyleClass().contains("summary-card"));
        assertTrue(card.getRoot().getStyleClass().contains("summary-interviewing"));
        assertCount(card, "0 / 0");
        assertEquals("shown / total", ((Text) card.getRoot().lookup(".summary-card-legend")).getText());
    }

    @Test
    public void constructor_emptyLists_includesEveryStatusWithZeroCounts() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        for (Status status : Status.values()) {
            StatusSummaryCard card = new StatusSummaryCard(status, persons, persons);
            assertEquals(status.toString(), ((Text) card.getRoot().lookup("#heading")).getText());
            assertEquals(status.name().toLowerCase(Locale.ROOT) + "SummaryCard", card.getRoot().getId());
            assertCount(card, "0 / 0");
        }
    }

    @Test
    public void counts_filterChanges_updatesShownAndRetainsOverallCounts() {
        ObservableList<Person> allPersons = FXCollections.observableArrayList();
        for (Status status : Status.values()) {
            allPersons.add(new PersonBuilder().withStatus(status).build());
        }
        FilteredList<Person> shownPersons = new FilteredList<>(allPersons);
        Map<Status, StatusSummaryCard> cards = new EnumMap<>(Status.class);
        for (Status status : Status.values()) {
            cards.put(status, new StatusSummaryCard(status, shownPersons, allPersons));
            assertCount(cards.get(status), "1 / 1");
        }

        shownPersons.setPredicate(person -> person.getStatus() == Status.INTERVIEWING);
        for (Status status : Status.values()) {
            assertCount(cards.get(status), status == Status.INTERVIEWING ? "1 / 1" : "0 / 1");
        }

        shownPersons.setPredicate(person -> false);
        for (StatusSummaryCard card : cards.values()) {
            assertCount(card, "0 / 1");
        }

        shownPersons.setPredicate(person -> true);
        for (StatusSummaryCard card : cards.values()) {
            assertCount(card, "1 / 1");
        }
    }

    @Test
    public void counts_modelLists_updatesAfterEditsExpansionAndReplacement() {
        ModelManager model = new ModelManager();
        ObservableList<Person> shownPersons = model.getFilteredPersonList();
        ObservableList<Person> allPersons = model.getAddressBook().getPersonList();
        StatusSummaryCard appliedCard = new StatusSummaryCard(Status.APPLIED, shownPersons, allPersons);
        StatusSummaryCard interviewingCard = new StatusSummaryCard(Status.INTERVIEWING, shownPersons, allPersons);
        StatusSummaryCard offeredCard = new StatusSummaryCard(Status.OFFERED, shownPersons, allPersons);
        Person applied = new PersonBuilder().withEmail("applied@example.com").build();
        Person interviewing = new PersonBuilder().withEmail("interviewing@example.com")
                .withStatus(Status.INTERVIEWING).build();
        model.addPerson(applied);
        model.addPerson(interviewing);
        model.updateFilteredPersonList(person -> person.getStatus() == Status.INTERVIEWING);
        assertCount(appliedCard, "0 / 1");
        assertCount(interviewingCard, "1 / 1");

        Person offered = new PersonBuilder(interviewing).withStatus(Status.OFFERED).build();
        model.setPerson(interviewing, offered);
        assertCount(interviewingCard, "0 / 0");
        assertCount(offeredCard, "0 / 1");

        model.updateFilteredPersonList(person -> true);
        model.expandPerson(offered);
        assertCount(offeredCard, "1 / 1");
        model.deletePerson(offered);
        assertCount(offeredCard, "0 / 0");
        model.setAddressBook(new AddressBook());
        assertCount(appliedCard, "0 / 0");
    }

    @Test
    public void counts_largeListAndClear_updatesDisplayedText() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        StatusSummaryCard applied = new StatusSummaryCard(Status.APPLIED, persons, persons);

        persons.setAll(Collections.nCopies(1000, new PersonBuilder().build()));
        assertCount(applied, "1000 / 1000");

        persons.clear();
        assertCount(applied, "0 / 0");
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        assertThrows(NullPointerException.class, () -> new StatusSummaryCard(null, persons, persons));
        assertThrows(NullPointerException.class, () -> new StatusSummaryCard(Status.APPLIED, null, persons));
        assertThrows(NullPointerException.class, () -> new StatusSummaryCard(Status.APPLIED, persons, null));
    }

    private static void assertCount(StatusSummaryCard card, String expected) {
        assertEquals(expected, card.countTextProperty().get());
    }
}
