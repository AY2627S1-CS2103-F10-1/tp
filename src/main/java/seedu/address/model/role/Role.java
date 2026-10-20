package seedu.address.model.role;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Represents an immutable catalogue entry with a stable identity across renames. */
public final class Role {
    public static final String MESSAGE_CONSTRAINTS = "Role names must not be blank or contain control characters.";
    public static final Role NO_ROLE = new Role("no-role", "No role");

    private final String id;
    private final String name;

    /** Creates a catalogue entry, validating stored identities as well as names. */
    public Role(String id, String name) {
        requireNonNull(id);
        requireNonNull(name);
        checkArgument(id.equals("no-role") || isUuid(id), "Invalid role identity.");
        this.name = validateName(name);
        checkArgument(id.equals("no-role") == normalize(this.name).equals(normalize("No role")),
                "The built-in No role identity and name are reserved.");
        checkArgument(!id.equals("no-role") || this.name.equals("No role"),
                "The built-in role must be named No role.");
        this.id = id;
    }

    /** Creates a new role with a generated identity. */
    public static Role create(String name) {
        return new Role(UUID.randomUUID().toString(), name);
    }

    /** Returns the shared lookup key; internal whitespace remains significant. */
    public static String normalize(String name) {
        return validateName(name).toLowerCase(Locale.ROOT);
    }

    private static String validateName(String name) {
        requireNonNull(name);
        String trimmed = name.strip();
        checkArgument(!trimmed.isEmpty() && name.codePoints().noneMatch(Character::isISOControl),
                MESSAGE_CONSTRAINTS);
        return trimmed;
    }

    private static boolean isUuid(String id) {
        try {
            return UUID.fromString(id).toString().equals(id);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Returns a renamed entry retaining its identity. */
    public Role renamed(String name) {
        checkArgument(!equals(NO_ROLE), "No role cannot be renamed or deleted.");
        return new Role(id, name);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Role role && id.equals(role.id) && name.equals(role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return name;
    }
}
