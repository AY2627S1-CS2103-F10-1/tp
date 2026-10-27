package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Collections;

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
    public void constructor_emptyLists_displaysTotalWithZeroCounts() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        CandidateSummaryCard card = new CandidateSummaryCard(persons, persons);

        assertEquals("Total candidates", ((Text) card.getRoot().lookup("#heading")).getText());
        assertEquals("totalSummaryCard", card.getRoot().getId());
        assertTrue(card.getRoot().getStyleClass().contains("summary-card"));
        assertTrue(card.getRoot().getStyleClass().contains("summary-total"));
        assertCount(card, "0 / 0");
        assertEquals("shown / total", ((Text) card.getRoot().lookup(".summary-card-legend")).getText());
    }

    @Test
    public void counts_filterChanges_countsCandidatesAcrossAllStatuses() {
        ObservableList<Person> allPersons = FXCollections.observableArrayList();
        for (Status status : Status.values()) {
            allPersons.add(new PersonBuilder().withStatus(status).build());
        }
        FilteredList<Person> shownPersons = new FilteredList<>(allPersons);
        CandidateSummaryCard total = new CandidateSummaryCard(shownPersons, allPersons);
        assertCount(total, allPersons.size() + " / " + allPersons.size());

        shownPersons.setPredicate(person -> person.getStatus() == Status.INTERVIEWING);
        assertCount(total, "1 / " + allPersons.size());

        shownPersons.setPredicate(person -> false);
        assertCount(total, "0 / " + allPersons.size());

        shownPersons.setPredicate(person -> true);
        assertCount(total, allPersons.size() + " / " + allPersons.size());
    }

    @Test
    public void counts_modelLists_updatesAfterEditsExpansionAndReplacement() {
        ModelManager model = new ModelManager();
        CandidateSummaryCard total = new CandidateSummaryCard(
                model.getFilteredPersonList(), model.getAddressBook().getPersonList());
        Person applied = new PersonBuilder().withEmail("applied@example.com").build();
        Person interviewing = new PersonBuilder().withEmail("interviewing@example.com")
                .withStatus(Status.INTERVIEWING).build();
        model.addPerson(applied);
        model.addPerson(interviewing);
        model.updateFilteredPersonList(person -> person.getStatus() == Status.INTERVIEWING);
        assertCount(total, "1 / 2");

        Person offered = new PersonBuilder(interviewing).withStatus(Status.OFFERED).build();
        model.setPerson(interviewing, offered);
        assertCount(total, "0 / 2");

        model.updateFilteredPersonList(person -> true);
        model.expandPerson(offered);
        assertCount(total, "1 / 2");
        model.deletePerson(offered);
        assertCount(total, "0 / 1");
        model.setAddressBook(new AddressBook());
        assertCount(total, "0 / 0");
    }

    @Test
    public void counts_largeListAndClear_updatesDisplayedText() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        CandidateSummaryCard total = new CandidateSummaryCard(persons, persons);

        persons.setAll(Collections.nCopies(1000, new PersonBuilder().build()));
        assertCount(total, "1000 / 1000");

        persons.clear();
        assertCount(total, "0 / 0");
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        assertThrows(NullPointerException.class, () -> new CandidateSummaryCard(null, persons));
        assertThrows(NullPointerException.class, () -> new CandidateSummaryCard(persons, null));
    }

    private static void assertCount(CandidateSummaryCard card, String expected) {
        assertEquals(expected, card.countTextProperty().get());
    }
}
