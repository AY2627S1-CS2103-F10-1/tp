package seedu.address.model.role;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/** Represents an immutable catalog entry with a stable identity across renames. */
public final class Role {
    public static final String MESSAGE_CONSTRAINTS =
            "Role names must not be blank or contain control characters (e.g. tabs, newlines, or carriage returns).";
    public static final String MESSAGE_RESERVED_SYNTAX =
            "Role names must not contain whitespace followed by n/, r/, p/, e/, a/, t/, no/, or s/ (ignoring case).";
    public static final String NO_ROLE_ID = "no-role";
    public static final String NO_ROLE_NAME = "No role";
    public static final Role NO_ROLE;

    private static final Pattern RESERVED_SYNTAX = Pattern.compile("(?i)\\s(?:n/|r/|p/|e/|a/|t/|no/|s/)");

    static {
        NO_ROLE = new Role(NO_ROLE_ID, NO_ROLE_NAME);
    }

    private final String id;
    private final String name;

    /**
     * Creates a catalog entry with a prepared display name, validating identities and names.
     *
     * @param id A valid custom UUID or the built-in role identity.
     * @param name A valid display name without surrounding whitespace.
     */
    public Role(String id, String name) {
        requireNonNull(id);
        requireNonNull(name);
        boolean isBuiltIn = id.equals(NO_ROLE_ID);
        checkArgument(isBuiltIn || isUuid(id), "Invalid role identity.");
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        checkArgument(hasNoReservedSyntax(name), MESSAGE_RESERVED_SYNTAX);
        boolean hasReservedName = toLookupKey(name).equals(toLookupKey(NO_ROLE_NAME));
        if (isBuiltIn) {
            checkArgument(hasReservedName, "The built-in " + NO_ROLE_NAME + " identity and name are reserved.");
            checkArgument(name.equals(NO_ROLE_NAME), "The built-in role must be named " + NO_ROLE_NAME + ".");
        } else {
            checkArgument(!hasReservedName, "The built-in " + NO_ROLE_NAME + " identity and name are reserved.");
        }
        this.name = name;
        this.id = id;
    }

    /** Creates a new role with a generated identity. */
    public static Role create(String name) {
        return new Role(UUID.randomUUID().toString(), prepareName(name));
    }

    /** Returns a stripped display name, preserving capitalization and internal whitespace. */
    public static String prepareName(String name) {
        return requireNonNull(name).strip();
    }

    /** Returns the case-insensitive lookup key; internal whitespace remains significant. */
    public static String toLookupKey(String name) {
        return prepareName(name).toLowerCase(Locale.ROOT);
    }

    /** Returns whether a name has no surrounding whitespace, is nonblank, and contains no control characters. */
    public static boolean isValidName(String name) {
        return !name.isEmpty() && name.equals(name.strip())
                && name.codePoints().noneMatch(Character::isISOControl);
    }

    /** Returns whether a name contains no reserved command-parameter syntax. */
    public static boolean hasNoReservedSyntax(String name) {
        return !RESERVED_SYNTAX.matcher(name).find();
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
        return new Role(id, prepareName(name));
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
