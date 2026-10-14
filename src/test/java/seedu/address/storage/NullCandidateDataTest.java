package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;

public class NullCandidateDataTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void toModelType_nullCandidate_throwsIllegalValueException() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                Arrays.asList(new JsonAdaptedPerson(ALICE), null));

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_NULL_CANDIDATE,
                data::toModelType);
    }

    @Test
    public void toModelType_nullTagName_throwsIllegalValueException() {
        JsonAdaptedTag tag = new JsonAdaptedTag((String) null);

        assertThrows(IllegalValueException.class, tag::toModelType);
    }

    @Test
    public void readAddressBook_nullCandidateData_throwsDataLoadingExceptionWithoutChangingFile()
            throws Exception {
        for (String json : List.of("null", "{\"persons\":[null]}", "{\"persons\":null}", "{}")) {
            assertInvalidFilePreserved(json);
        }
    }

    @Test
    public void readAddressBook_nullCandidateAmongValidCandidates_throwsDataLoadingException() throws Exception {
        String candidate = JsonUtil.toJsonString(new JsonAdaptedPerson(ALICE));

        assertInvalidFilePreserved("{\"persons\":[null," + candidate + ",null]}");
    }

    @Test
    public void readAddressBook_nullTagAmongValidTags_throwsDataLoadingException() throws Exception {
        JsonAdaptedPerson candidate = new JsonAdaptedPerson(ALICE.getName().fullName, ALICE.getPhone().value,
                ALICE.getEmail().value, ALICE.getAddress().value,
                Arrays.asList(new JsonAdaptedTag("friend"), null));

        assertInvalidFilePreserved("{\"persons\":[" + JsonUtil.toJsonString(candidate) + "]}");
    }

    @Test
    public void readAddressBook_emptyCandidates_loadsEmptyList() throws Exception {
        Path path = temporaryFolder.resolve("empty.json");
        Files.writeString(path, "{\"persons\":[]}");

        assertEquals(List.of(), new JsonAddressBookStorage(path).readAddressBook().orElseThrow().getPersonList());
    }

    /**
     * Verifies that malformed records use the checked loading failure without changing their source file.
     */
    private void assertInvalidFilePreserved(String json) throws Exception {
        Path path = temporaryFolder.resolve("invalid.json");
        Files.writeString(path, json);

        assertThrows(DataLoadingException.class, new JsonAddressBookStorage(path)::readAddressBook);
        assertEquals(json, Files.readString(path));
    }
}
