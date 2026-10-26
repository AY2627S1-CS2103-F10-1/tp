package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.testutil.PersonBuilder;

public class CandidateSummaryTest {
    @Test
    public void constructor_nullLists_throwsNullPointerException() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        assertThrows(NullPointerException.class, () -> new CandidateSummary(null, persons));
        assertThrows(NullPointerException.class, () -> new CandidateSummary(persons, null));
    }

    @Test
    public void constructor_emptyLists_exposesZeroCountsForEveryStatus() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        CandidateSummary summary = new CandidateSummary(persons, persons);

        assertCounts(summary.getTotalCounts(), 0, 0);
        for (Status status : Status.values()) {
            assertCounts(summary.getStatusCounts(status), 0, 0);
        }
    }

    @Test
    public void counts_nullObservables_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CandidateSummary.Counts(null, new SimpleLongProperty()));
        assertThrows(NullPointerException.class, () -> new CandidateSummary.Counts(new SimpleLongProperty(), null));
    }

    @Test
    public void getStatusCounts_nullStatus_throwsNullPointerException() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        CandidateSummary summary = new CandidateSummary(persons, persons);

        assertThrows(NullPointerException.class, () -> summary.getStatusCounts(null));
    }

    @Test
    public void counts_filterChanges_updatesShownAndRetainsOverallCounts() {
        ObservableList<Person> allPersons = FXCollections.observableArrayList();
        for (Status status : Status.values()) {
            allPersons.add(new PersonBuilder().withStatus(status).build());
        }
        FilteredList<Person> shownPersons = new FilteredList<>(allPersons);
        CandidateSummary summary = new CandidateSummary(shownPersons, allPersons);

        assertCounts(summary.getTotalCounts(), allPersons.size(), allPersons.size());
        for (Status status : Status.values()) {
            assertCounts(summary.getStatusCounts(status), 1, 1);
        }

        shownPersons.setPredicate(person -> person.getStatus() == Status.INTERVIEWING);
        assertCounts(summary.getTotalCounts(), 1, allPersons.size());
        assertCounts(summary.getStatusCounts(Status.INTERVIEWING), 1, 1);
        assertCounts(summary.getStatusCounts(Status.APPLIED), 0, 1);

        shownPersons.setPredicate(person -> false);
        assertCounts(summary.getTotalCounts(), 0, allPersons.size());
        for (Status status : Status.values()) {
            assertCounts(summary.getStatusCounts(status), 0, 1);
        }

        shownPersons.setPredicate(person -> true);
        assertCounts(summary.getTotalCounts(), allPersons.size(), allPersons.size());
    }

    @Test
    public void counts_modelLists_updatesAfterEditsExpansionAndReplacement() {
        ModelManager model = new ModelManager();
        CandidateSummary summary = new CandidateSummary(
                model.getFilteredPersonList(), model.getAddressBook().getPersonList());
        Person applied = new PersonBuilder().withEmail("applied@example.com").build();
        Person interviewing = new PersonBuilder().withEmail("interviewing@example.com")
                .withStatus(Status.INTERVIEWING).build();
        model.addPerson(applied);
        model.addPerson(interviewing);
        model.updateFilteredPersonList(person -> person.getStatus() == Status.INTERVIEWING);
        assertCounts(summary.getTotalCounts(), 1, 2);
        assertCounts(summary.getStatusCounts(Status.APPLIED), 0, 1);

        Person offered = new PersonBuilder(interviewing).withStatus(Status.OFFERED).build();
        model.setPerson(interviewing, offered);
        assertCounts(summary.getTotalCounts(), 0, 2);
        assertCounts(summary.getStatusCounts(Status.INTERVIEWING), 0, 0);
        assertCounts(summary.getStatusCounts(Status.OFFERED), 0, 1);

        model.updateFilteredPersonList(person -> true);
        model.expandPerson(offered);
        assertCounts(summary.getTotalCounts(), 1, 2);
        assertCounts(summary.getStatusCounts(Status.OFFERED), 1, 1);
        model.deletePerson(offered);
        assertCounts(summary.getTotalCounts(), 0, 1);
        model.setAddressBook(new AddressBook());
        assertCounts(summary.getTotalCounts(), 0, 0);
        assertCounts(summary.getStatusCounts(Status.APPLIED), 0, 0);
    }

    private static void assertCounts(CandidateSummary.Counts counts, long shown, long total) {
        assertEquals(shown, counts.shown().get());
        assertEquals(total, counts.total().get());
    }
}
