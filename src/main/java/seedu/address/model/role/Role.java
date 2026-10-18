package seedu.address.model.role;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Role a candidate is applying for.
 * Guarantees: immutable; name is valid as declared in {@link #isValidRoleName(String)}.
 */
public class Role {

    public static final String MESSAGE_CONSTRAINTS =
            "Role names should contain only alphanumeric characters, spaces, hyphens, and slashes.";
    public static final String VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} \\-/]*";

    public final String roleName;

    /**
     * Constructs a {@code Role}.
     *
     * @param roleName A valid role name.
     */
    public Role(String roleName) {
        requireNonNull(roleName);
        String trimmed = roleName.trim();
        checkArgument(isValidRoleName(trimmed), MESSAGE_CONSTRAINTS);
        this.roleName = trimmed;
    }

    /**
     * Returns true if a given string is a valid role name.
     */
    public static boolean isValidRoleName(String test) {
        return test != null && test.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Role otherRole)) {
            return false;
        }
        return roleName.equalsIgnoreCase(otherRole.roleName);
    }

    @Override
    public int hashCode() {
        return roleName.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return '[' + roleName + ']';
    }
}
