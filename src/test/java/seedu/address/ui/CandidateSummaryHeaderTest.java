package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.testutil.PersonBuilder;

public class CandidateSummaryHeaderTest {
    @Test
    public void constructor_nullLists_throwsNullPointerException() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        assertThrows(NullPointerException.class, () -> new CandidateSummaryHeader(null, persons));
        assertThrows(NullPointerException.class, () -> new CandidateSummaryHeader(persons, null));
    }

    @Test
    public void constructor_emptyLists_includesAllStatusesInEnumOrder() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        CandidateSummaryHeader header = new CandidateSummaryHeader(persons, persons);
        Region cards = cards(header);

        assertEquals(Status.values().length + 1, cards.getChildrenUnmodifiable().size());
        assertEquals("totalSummaryCard", cards.getChildrenUnmodifiable().get(0).getId());
        assertCount(header, "total", "0 / 0");
        for (Status status : Status.values()) {
            String id = status.name().toLowerCase(Locale.ROOT);
            assertEquals(id + "SummaryCard", cards.getChildrenUnmodifiable().get(status.ordinal() + 1).getId());
            assertCount(header, id, "0 / 0");
        }
    }

    @Test
    public void counts_filterChanges_updatesShownAndRetainsOverallCounts() {
        ObservableList<Person> allPersons = FXCollections.observableArrayList();
        for (Status status : Status.values()) {
            allPersons.add(new PersonBuilder().withStatus(status).build());
        }
        FilteredList<Person> shownPersons = new FilteredList<>(allPersons);
        CandidateSummaryHeader header = new CandidateSummaryHeader(shownPersons, allPersons);

        assertCount(header, "total", allPersons.size() + " / " + allPersons.size());
        for (Status status : Status.values()) {
            assertCount(header, status.name().toLowerCase(Locale.ROOT), "1 / 1");
        }

        shownPersons.setPredicate(person -> person.getStatus() == Status.INTERVIEWING);
        assertCount(header, "total", "1 / " + allPersons.size());
        assertCount(header, "interviewing", "1 / 1");
        assertCount(header, "applied", "0 / 1");

        shownPersons.setPredicate(person -> false);
        assertCount(header, "total", "0 / " + allPersons.size());
        for (Status status : Status.values()) {
            assertCount(header, status.name().toLowerCase(Locale.ROOT), "0 / 1");
        }

        shownPersons.setPredicate(person -> true);
        assertCount(header, "total", allPersons.size() + " / " + allPersons.size());
    }

    @Test
    public void counts_dataChangesWhileHidden_updatesBothCounts() {
        ObservableList<Person> allPersons = FXCollections.observableArrayList();
        FilteredList<Person> shownPersons = new FilteredList<>(allPersons,
                person -> person.getStatus() == Status.INTERVIEWING);
        CandidateSummaryHeader header = new CandidateSummaryHeader(shownPersons, allPersons);
        layout(header, header.minWidth(-1));
        assertFalse(cards(header).isVisible());

        Person interviewing = new PersonBuilder().withStatus(Status.INTERVIEWING).build();
        allPersons.add(interviewing);
        assertCount(header, "total", "1 / 1");
        assertCount(header, "interviewing", "1 / 1");

        Person offered = new PersonBuilder(interviewing).withStatus(Status.OFFERED).build();
        allPersons.set(0, offered);
        assertCount(header, "total", "0 / 1");
        assertCount(header, "interviewing", "0 / 0");
        assertCount(header, "offered", "0 / 1");

        allPersons.add(interviewing);
        allPersons.remove(offered);
        assertCount(header, "total", "1 / 1");
        assertCount(header, "offered", "0 / 0");

        allPersons.setAll(offered);
        assertCount(header, "total", "0 / 1");
        assertCount(header, "offered", "0 / 1");

        allPersons.clear();
        layout(header, header.prefWidth(-1));
        assertTrue(cards(header).isVisible());
        assertCount(header, "total", "0 / 0");
        assertCount(header, "offered", "0 / 0");
    }

    @Test
    public void counts_modelLists_updatesAfterEditsExpansionAndReplacement() {
        ModelManager model = new ModelManager();
        CandidateSummaryHeader header = new CandidateSummaryHeader(
                model.getFilteredPersonList(), model.getAddressBook().getPersonList());
        Person applied = new PersonBuilder().withEmail("applied@example.com").build();
        Person interviewing = new PersonBuilder().withEmail("interviewing@example.com")
                .withStatus(Status.INTERVIEWING).build();
        model.addPerson(applied);
        model.addPerson(interviewing);
        model.updateFilteredPersonList(person -> person.getStatus() == Status.INTERVIEWING);
        assertCount(header, "total", "1 / 2");
        assertCount(header, "applied", "0 / 1");

        Person offered = new PersonBuilder(interviewing).withStatus(Status.OFFERED).build();
        model.setPerson(interviewing, offered);
        assertCount(header, "total", "0 / 2");
        assertCount(header, "interviewing", "0 / 0");
        assertCount(header, "offered", "0 / 1");

        model.updateFilteredPersonList(person -> true);
        model.expandPerson(offered);
        assertCount(header, "total", "1 / 2");
        assertCount(header, "offered", "1 / 1");
        model.deletePerson(offered);
        assertCount(header, "total", "0 / 1");
        model.setAddressBook(new AddressBook());
        assertCount(header, "total", "0 / 0");
        assertCount(header, "applied", "0 / 0");
    }

    @Test
    public void layout_widthCrossesThreshold_showsAllOrNoneWithoutOscillation() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        CandidateSummaryHeader header = new CandidateSummaryHeader(persons, persons);
        double requiredWidth = header.prefWidth(-1);
        Region cards = cards(header);

        layout(header, requiredWidth - 1);
        assertFalse(cards.isVisible());
        assertFalse(cards.isManaged());
        assertTrue(header.getChildrenUnmodifiable().get(0).isVisible());
        assertEquals(requiredWidth, header.prefWidth(-1));

        layout(header, requiredWidth);
        assertTrue(cards.isVisible());
        assertTrue(cards.isManaged());
        for (Node card : cards.getChildrenUnmodifiable()) {
            assertTrue(card.isVisible());
            assertTrue(((Region) card).getWidth() >= card.minWidth(-1));
        }
        assertTrue(cards.getLayoutX() + cards.getWidth() <= header.getWidth());

        layout(header, requiredWidth + 1);
        assertTrue(cards.isVisible());
        layout(header, requiredWidth - 1);
        assertFalse(cards.isVisible());
        layout(header, requiredWidth);
        assertTrue(cards.isVisible());
    }

    @Test
    public void layout_wideCountText_increasesRequiredWidthWithoutClipping() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        CandidateSummaryHeader header = new CandidateSummaryHeader(persons, persons);
        Text count = (Text) header.lookup("#totalCount");
        double originalWidth = header.prefWidth(-1);
        layout(header, header.minWidth(-1));
        header.layout();
        assertFalse(header.isNeedsLayout());
        count.textProperty().unbind();
        count.setText("2147483647 / 2147483647");

        assertTrue(header.isNeedsLayout());
        double requiredWidth = header.prefWidth(-1);
        assertTrue(requiredWidth > originalWidth);
        layout(header, originalWidth);
        assertFalse(cards(header).isVisible());
        layout(header, requiredWidth);
        assertTrue(cards(header).isVisible());
        Region card = (Region) header.lookup("#totalSummaryCard");
        assertTrue(card.getWidth() >= count.getLayoutBounds().getWidth());

        count.setText("0 / 0");
        assertEquals(originalWidth, header.prefWidth(-1));
        layout(header, originalWidth);
        assertTrue(cards(header).isVisible());
    }

    private static Region cards(CandidateSummaryHeader header) {
        return (Region) header.lookup("#summaryCards");
    }

    private static void assertCount(CandidateSummaryHeader header, String id, String expected) {
        assertEquals(expected, ((Text) header.lookup("#" + id + "Count")).getText());
    }

    private static void layout(CandidateSummaryHeader header, double width) {
        header.resize(width, header.prefHeight(width));
        header.requestLayout();
        header.layout();
    }
}
