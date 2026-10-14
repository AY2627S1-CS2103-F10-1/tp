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
    public Path temporaryFolder;

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
}
