package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void getNote_newPerson_returnsEmptyNote() {
        assertTrue(ALICE.getNote().isEmpty());
        assertFalse(ALICE.hasNote());
    }

    @Test
    public void equals_differentNotes_changesEqualityButNotIdentity() {
        Person notedAlice = new PersonBuilder(ALICE).withNote("Interview feedback").build();
        Person copy = new PersonBuilder(notedAlice).build();

        assertTrue(notedAlice.hasNote());
        assertEquals(new Note("Interview feedback"), notedAlice.getNote().orElseThrow());
        assertTrue(ALICE.isSamePerson(notedAlice));
        assertTrue(notedAlice.isSamePerson(ALICE));
        assertFalse(ALICE.equals(notedAlice));
        assertFalse(notedAlice.equals(new PersonBuilder(ALICE).withNote("Other feedback").build()));
        assertEquals(notedAlice, copy);
        assertEquals(notedAlice.hashCode(), copy.hashCode());
        assertFalse(notedAlice.toString().contains("Interview feedback"));
    }

    @Test
    public void constructor_nullNote_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getAddress(), ALICE.getTags(), Status.APPLIED, null));
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson_selfOrNull_returnsExpectedResult() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

    }

    @Test
    public void isSamePerson_sameEmailDifferentDetails_returnsTrue() {
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).withStatus(Status.OFFERED)
                .withNote("Interview feedback").build();

        assertTrue(ALICE.isSamePerson(editedAlice));
        assertTrue(editedAlice.isSamePerson(ALICE));
    }

    @Test
    public void isSamePerson_sameNameDifferentEmail_returnsFalse() {
        Person editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();

        assertFalse(ALICE.isSamePerson(editedAlice));
        assertFalse(editedAlice.isSamePerson(ALICE));
    }

    @Test
    public void isSamePerson_emailCaseDiffers_returnsTrueButNotEqual() {
        Person lowerCase = new PersonBuilder().withEmail("alice@example.com").build();
        Person mixedCase = new PersonBuilder(lowerCase).withEmail("AlIcE@ExAmPlE.CoM").build();

        assertTrue(lowerCase.isSamePerson(mixedCase));
        assertTrue(mixedCase.isSamePerson(lowerCase));
        assertFalse(lowerCase.equals(mixedCase));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));

        // different status -> returns false
        editedAlice = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getAddress(), ALICE.getTags(), Status.OFFERED);
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress() + ", status="
                + ALICE.getStatus() + ", tags=" + ALICE.getTags() + "}";
        assertEquals(expected, ALICE.toString());
    }

    @Test
    public void hashCode_equalPersons_returnsEqualHashCode() {
        Person aliceCopy = new PersonBuilder(ALICE).build();

        assertEquals(ALICE.hashCode(), aliceCopy.hashCode());
    }
}
