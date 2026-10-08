package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests ACL copying on every platform, alongside the native Windows storage tests.
 */
public class JsonAddressBookStorageAclTest {
    private static final UserPrincipal OWNER = () -> "candidate-owner";
    private static final AclEntry DENY_WRITE = AclEntry.newBuilder()
            .setType(AclEntryType.DENY)
            .setPrincipal(OWNER)
            .setPermissions(AclEntryPermission.WRITE_DATA)
            .build();
    private static final AclEntry ALLOW_READ = AclEntry.newBuilder()
            .setType(AclEntryType.ALLOW)
            .setPrincipal(OWNER)
            .setPermissions(AclEntryPermission.READ_DATA)
            .build();
    private static final List<AclEntry> EXISTING_ACL = List.of(DENY_WRITE, ALLOW_READ);
    private static final List<AclEntry> DEFAULT_ACL = List.of(ALLOW_READ);

    @Test
    public void copyExistingAcl_existingAcl_preservesEntriesAndOrder() throws Exception {
        AclFileAttributeViewStub destination = new AclFileAttributeViewStub(EXISTING_ACL);
        AclFileAttributeViewStub temporary = new AclFileAttributeViewStub(DEFAULT_ACL);

        JsonAddressBookStorage.copyExistingAcl(destination, temporary);

        assertEquals(EXISTING_ACL, temporary.getAcl());
        assertEquals(EXISTING_ACL, destination.getAcl());
        assertTrue(temporary.wasAclSet);
    }

    @Test
    public void copyExistingAcl_emptyExistingAcl_clearsTemporaryAcl() throws Exception {
        AclFileAttributeViewStub destination = new AclFileAttributeViewStub(List.of());
        AclFileAttributeViewStub temporary = new AclFileAttributeViewStub(DEFAULT_ACL);

        JsonAddressBookStorage.copyExistingAcl(destination, temporary);

        assertEquals(List.of(), temporary.getAcl());
        assertTrue(temporary.wasAclSet);
    }

    @Test
    public void copyExistingAcl_unsupportedAcl_keepsDefaultAcl() throws Exception {
        AclFileAttributeViewStub temporary = new AclFileAttributeViewStub(DEFAULT_ACL);

        JsonAddressBookStorage.copyExistingAcl(null, temporary);

        assertEquals(DEFAULT_ACL, temporary.getAcl());
        assertFalse(temporary.wasAclSet);
    }

    @Test
    public void copyExistingAcl_missingDestination_keepsDefaultAcl() throws Exception {
        AclFileAttributeViewStub destination = new AclFileAttributeViewStub(EXISTING_ACL);
        destination.readFailure = new NoSuchFileException("missing-candidates.json");
        AclFileAttributeViewStub temporary = new AclFileAttributeViewStub(DEFAULT_ACL);

        JsonAddressBookStorage.copyExistingAcl(destination, temporary);

        assertEquals(DEFAULT_ACL, temporary.getAcl());
        assertFalse(temporary.wasAclSet);
    }

    @Test
    public void copyExistingAcl_readFailure_throwsIoExceptionAndKeepsDefaultAcl() throws Exception {
        AclFileAttributeViewStub destination = new AclFileAttributeViewStub(EXISTING_ACL);
        destination.readFailure = new IOException("Cannot read ACL");
        AclFileAttributeViewStub temporary = new AclFileAttributeViewStub(DEFAULT_ACL);

        assertThrows(IOException.class, "Cannot read ACL", () ->
                JsonAddressBookStorage.copyExistingAcl(destination, temporary));

        assertEquals(DEFAULT_ACL, temporary.getAcl());
        assertFalse(temporary.wasAclSet);
    }

    @Test
    public void copyExistingAcl_writeFailure_throwsIoExceptionAndKeepsExistingAcl() throws Exception {
        AclFileAttributeViewStub destination = new AclFileAttributeViewStub(EXISTING_ACL);
        AclFileAttributeViewStub temporary = new AclFileAttributeViewStub(DEFAULT_ACL);
        temporary.writeFailure = new IOException("Cannot apply ACL");

        assertThrows(IOException.class, "Cannot apply ACL", () ->
                JsonAddressBookStorage.copyExistingAcl(destination, temporary));

        assertEquals(EXISTING_ACL, destination.getAcl());
        assertEquals(DEFAULT_ACL, temporary.getAcl());
        assertFalse(temporary.wasAclSet);
    }

    /**
     * Supplies ACLs and deterministic metadata failures without requiring native ACL support.
     */
    private static class AclFileAttributeViewStub implements AclFileAttributeView {
        private List<AclEntry> acl;
        private IOException readFailure;
        private IOException writeFailure;
        private boolean wasAclSet;

        AclFileAttributeViewStub(List<AclEntry> acl) {
            this.acl = List.copyOf(acl);
        }

        @Override
        public String name() {
            return "acl";
        }

        @Override
        public List<AclEntry> getAcl() throws IOException {
            if (readFailure != null) {
                throw readFailure;
            }
            return new ArrayList<>(acl);
        }

        @Override
        public void setAcl(List<AclEntry> acl) throws IOException {
            if (writeFailure != null) {
                throw writeFailure;
            }
            this.acl = List.copyOf(acl);
            wasAclSet = true;
        }

        @Override
        public UserPrincipal getOwner() {
            throw new AssertionError("ACL copying must not read the file owner.");
        }

        @Override
        public void setOwner(UserPrincipal owner) {
            throw new AssertionError("ACL copying must not change the file owner.");
        }
    }
}
