package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.testutil.PersonBuilder;

public class CandidateDataNormalizationTest {
    @TempDir
    Path temporaryFolder;

    @Test
    public void readAndSaveAddressBook_emptyNoteAndPaddedStatus_preservesAllCandidates() throws Exception {
        Path path = temporaryFolder.resolve("candidates.json");
        for (String note : List.of("", " \t ")) {
            JsonAdaptedPerson edited = new JsonAdaptedPerson(BENSON.getName().fullName, BENSON.getPhone().value,
                    BENSON.getEmail().value, BENSON.getAddress().value, List.of(), " Applied ", note);
            Files.writeString(path, "{\"persons\":[" + JsonUtil.toJsonString(new JsonAdaptedPerson(ALICE))
                    + "," + JsonUtil.toJsonString(edited) + "]}");
            JsonAddressBookStorage storage = new JsonAddressBookStorage(path);

            AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());

            assertEquals(2, loaded.getPersonList().size());
            assertEquals(ALICE, loaded.getPersonList().getFirst());
            assertTrue(loaded.getPersonList().get(1).getNote().isEmpty());
            assertEquals(Status.APPLIED, loaded.getPersonList().get(1).getStatus());
            storage.saveAddressBook(loaded);
            assertEquals(loaded, storage.readAddressBook().orElseThrow());
        }
    }

    @Test
    public void toModelType_paddedStatusAndValidNote_preservesBoth() throws Exception {
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(BENSON.getName().fullName, BENSON.getPhone().value,
                BENSON.getEmail().value, BENSON.getAddress().value,
                BENSON.getTags().stream().map(JsonAdaptedTag::new).toList(), " \t oFfErEd \n", "Follow up");
        Person expected = new PersonBuilder(BENSON).withStatus(Status.OFFERED).withNote("Follow up").build();

        assertEquals(expected, adapted.toModelType());
    }

    @Test
    public void toModelType_paddedTextFields_trimsLikeCommands() throws Exception {
        JsonAdaptedPerson adapted = new JsonAdaptedPerson("  Benson Meier ", " 98765432\t",
                " johnd@example.com ", " 311, Clementi Ave 2, #02-25 ",
                List.of(new JsonAdaptedTag(" owesMoney "), new JsonAdaptedTag("friends\t")), null, null);

        assertEquals(BENSON, adapted.toModelType());
    }

    @Test
    public void readAddressBook_byteOrderMark_loadsAllCandidates() throws Exception {
        Path path = temporaryFolder.resolve("candidates.json");
        String json = "{\"persons\":[" + JsonUtil.toJsonString(new JsonAdaptedPerson(ALICE)) + ","
                + JsonUtil.toJsonString(new JsonAdaptedPerson(BENSON)) + "]}";
        // Notepad's "UTF-8 with BOM" option starts the file with this mark.
        Files.writeString(path, "\uFEFF" + json);

        AddressBook loaded = new AddressBook(
                new JsonAddressBookStorage(path).readAddressBook().orElseThrow());

        assertEquals(List.of(ALICE, BENSON), loaded.getPersonList());
    }
}
