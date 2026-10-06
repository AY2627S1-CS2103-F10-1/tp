package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class NoteStorageTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void readAndSaveAddressBook_notesWithSpecialCharacters_preservesContents() throws Exception {
        String text = "Feedback: \"C++\", [SQL] \\ follow-up / next\tYES\n\u4e2d\u6587 \ud83d\ude00";
        AddressBook addressBook = getTypicalAddressBook();
        Person candidate = new PersonBuilder(ALICE).withNote(text).build();
        addressBook.setPerson(ALICE, candidate);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("notes.json"));

        storage.saveAddressBook(addressBook);

        assertEquals(addressBook, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void readAddressBook_blankOrOverlongNote_throwsDataLoadingException() throws Exception {
        Path path = temporaryFolder.resolve("invalid-note.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        for (String note : new String[] {"", " \t ", "x".repeat(501)}) {
            JsonAdaptedPerson adapted = new JsonAdaptedPerson(ALICE.getName().fullName, ALICE.getPhone().value,
                    ALICE.getEmail().value, ALICE.getAddress().value, List.of(), note);
            Files.writeString(path, "{\"persons\":[" + JsonUtil.toJsonString(adapted) + "]}");

            assertThrows(DataLoadingException.class, storage::readAddressBook);
        }
    }

    @Test
    public void readAddressBook_legacyJsonWithoutNotes_loadsExistingCandidates() throws Exception {
        Path path = Path.of("src", "test", "data", "JsonSerializableAddressBookTest", "typicalPersonsAddressBook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);

        AddressBook restored = new AddressBook(storage.readAddressBook().orElseThrow());

        assertEquals(getTypicalAddressBook(), restored);
        assertTrue(restored.getPersonList().stream().noneMatch(Person::hasNote));
    }

}
