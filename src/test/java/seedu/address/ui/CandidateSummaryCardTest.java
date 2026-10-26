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

public class CandidateSummaryCardTest {
    @Test
    public void constructor_statusCard_setsTitleIdsAndStyle() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        CandidateSummaryCard card = new CandidateSummaryCard(Status.INTERVIEWING, persons, persons);

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
        CandidateSummaryCard total = new CandidateSummaryCard(persons, persons);
        assertEquals("Total candidates", ((Text) total.getRoot().lookup("#heading")).getText());
        assertEquals("totalSummaryCard", total.getRoot().getId());
        assertCount(total, "0 / 0");

        for (Status status : Status.values()) {
            CandidateSummaryCard card = new CandidateSummaryCard(status, persons, persons);
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
        CandidateSummaryCard total = new CandidateSummaryCard(shownPersons, allPersons);
        Map<Status, CandidateSummaryCard> cards = new EnumMap<>(Status.class);
        for (Status status : Status.values()) {
            cards.put(status, new CandidateSummaryCard(status, shownPersons, allPersons));
            assertCount(cards.get(status), "1 / 1");
        }
        assertCount(total, allPersons.size() + " / " + allPersons.size());

        shownPersons.setPredicate(person -> person.getStatus() == Status.INTERVIEWING);
        assertCount(total, "1 / " + allPersons.size());
        for (Status status : Status.values()) {
            assertCount(cards.get(status), status == Status.INTERVIEWING ? "1 / 1" : "0 / 1");
        }

        shownPersons.setPredicate(person -> false);
        assertCount(total, "0 / " + allPersons.size());
        for (CandidateSummaryCard card : cards.values()) {
            assertCount(card, "0 / 1");
        }

        shownPersons.setPredicate(person -> true);
        assertCount(total, allPersons.size() + " / " + allPersons.size());
        for (CandidateSummaryCard card : cards.values()) {
            assertCount(card, "1 / 1");
        }
    }

    @Test
    public void counts_modelLists_updatesAfterEditsExpansionAndReplacement() {
        ModelManager model = new ModelManager();
        ObservableList<Person> shownPersons = model.getFilteredPersonList();
        ObservableList<Person> allPersons = model.getAddressBook().getPersonList();
        CandidateSummaryCard total = new CandidateSummaryCard(shownPersons, allPersons);
        CandidateSummaryCard appliedCard = new CandidateSummaryCard(Status.APPLIED, shownPersons, allPersons);
        CandidateSummaryCard interviewingCard = new CandidateSummaryCard(Status.INTERVIEWING, shownPersons, allPersons);
        CandidateSummaryCard offeredCard = new CandidateSummaryCard(Status.OFFERED, shownPersons, allPersons);
        Person applied = new PersonBuilder().withEmail("applied@example.com").build();
        Person interviewing = new PersonBuilder().withEmail("interviewing@example.com")
                .withStatus(Status.INTERVIEWING).build();
        model.addPerson(applied);
        model.addPerson(interviewing);
        model.updateFilteredPersonList(person -> person.getStatus() == Status.INTERVIEWING);
        assertCount(total, "1 / 2");
        assertCount(appliedCard, "0 / 1");

        Person offered = new PersonBuilder(interviewing).withStatus(Status.OFFERED).build();
        model.setPerson(interviewing, offered);
        assertCount(total, "0 / 2");
        assertCount(interviewingCard, "0 / 0");
        assertCount(offeredCard, "0 / 1");

        model.updateFilteredPersonList(person -> true);
        model.expandPerson(offered);
        assertCount(total, "1 / 2");
        assertCount(offeredCard, "1 / 1");
        model.deletePerson(offered);
        assertCount(total, "0 / 1");
        model.setAddressBook(new AddressBook());
        assertCount(total, "0 / 0");
        assertCount(appliedCard, "0 / 0");
    }

    @Test
    public void counts_largeListAndClear_updatesDisplayedText() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        CandidateSummaryCard total = new CandidateSummaryCard(persons, persons);
        CandidateSummaryCard applied = new CandidateSummaryCard(Status.APPLIED, persons, persons);

        persons.setAll(Collections.nCopies(1000, new PersonBuilder().build()));
        assertCount(total, "1000 / 1000");
        assertCount(applied, "1000 / 1000");

        persons.clear();
        assertCount(total, "0 / 0");
        assertCount(applied, "0 / 0");
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        assertThrows(NullPointerException.class, () -> new CandidateSummaryCard(null, persons));
        assertThrows(NullPointerException.class, () -> new CandidateSummaryCard(persons, null));
        assertThrows(NullPointerException.class, () -> new CandidateSummaryCard(null, persons, persons));
        assertThrows(NullPointerException.class, () -> new CandidateSummaryCard(Status.APPLIED, null, persons));
        assertThrows(NullPointerException.class, () -> new CandidateSummaryCard(Status.APPLIED, persons, null));
    }

    private static void assertCount(CandidateSummaryCard card, String expected) {
        assertEquals(expected, card.countTextProperty().get());
    }
}
