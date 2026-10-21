package seedu.address.model.role;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Represents an immutable catalogue entry with a stable identity across renames. */
public final class Role {
    public static final String MESSAGE_CONSTRAINTS =
            "Role names must not be blank or contain control characters (e.g. tabs, newlines, or carriage returns).";
    public static final String NO_ROLE_ID = "no-role";
    public static final String NO_ROLE_NAME = "No role";
    public static final Role NO_ROLE = new Role(NO_ROLE_ID, NO_ROLE_NAME);

    private final String id;
    private final String name;

    /** Creates a catalogue entry, validating stored identities as well as names. */
    public Role(String id, String name) {
        requireNonNull(id);
        requireNonNull(name);
        boolean isBuiltIn = id.equals(NO_ROLE_ID);
        checkArgument(isBuiltIn || isUuid(id), "Invalid role identity.");
        this.name = validateName(name);
        checkArgument(isBuiltIn == normalize(this.name).equals(normalize(NO_ROLE_NAME)),
                "The built-in " + NO_ROLE_NAME + " identity and name are reserved.");
        checkArgument(!isBuiltIn || this.name.equals(NO_ROLE_NAME),
                "The built-in role must be named " + NO_ROLE_NAME + ".");
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
        checkArgument(!trimmed.isEmpty() && trimmed.codePoints().noneMatch(Character::isISOControl),
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
        checkArgument(!equals(NO_ROLE), NO_ROLE_NAME + " cannot be renamed or deleted.");
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
