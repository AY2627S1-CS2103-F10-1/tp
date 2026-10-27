package seedu.address.model.role;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Validates and exposes an immutable role catalog, including unused roles. */
public final class RoleCatalog {
    public static final String MESSAGE_ROLES_REQUIRED = "Every person must have at least one role.";
    public static final String MESSAGE_DUPLICATE_NAME = "A role with this name already exists.";
    public static final String MESSAGE_UNKNOWN_ROLE = "Role does not exist: %1$s";

    private final List<Role> roles;

    /** Creates the initial catalog containing only the built-in role. */
    public RoleCatalog() {
        this(List.of(Role.NO_ROLE));
    }

    /** Creates a validated catalog; duplicate names and identities are rejected. */
    public RoleCatalog(Collection<Role> roles) {
        requireNonNull(roles);
        Set<String> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (Role role : roles) {
            requireNonNull(role);
            checkArgument(ids.add(role.getId()), "Duplicate role identity.");
            checkArgument(names.add(Role.toLookupKey(role.getName())), MESSAGE_DUPLICATE_NAME);
        }
        checkArgument(roles.contains(Role.NO_ROLE), "The catalog must contain No role.");
        this.roles = roles.stream().sorted(Comparator.comparing(role -> Role.toLookupKey(role.getName()))).toList();
    }

    public List<Role> getRoles() {
        return roles;
    }

    /** Finds a role by its trimmed, case-insensitive name. */
    public Optional<Role> find(String name) {
        String preparedName = Role.prepareName(name);
        checkArgument(Role.isValidName(preparedName), Role.MESSAGE_CONSTRAINTS);
        checkArgument(Role.hasNoReservedSyntax(preparedName), Role.MESSAGE_RESERVED_SYNTAX);
        String key = Role.toLookupKey(preparedName);
        return roles.stream().filter(role -> Role.toLookupKey(role.getName()).equals(key)).findFirst();
    }

    /** Resolves an existing name, rejecting unknown assignments. */
    public Role requireRole(String name) {
        return find(name).orElseThrow(() ->
                new IllegalArgumentException(String.format(MESSAGE_UNKNOWN_ROLE, name.strip())));
    }

    /** Resolves a nonempty set of names through the same lookup used by management commands. */
    public Set<Role> resolve(Collection<String> names) {
        requireNonNull(names);
        checkArgument(!names.isEmpty(), MESSAGE_ROLES_REQUIRED);
        Set<Role> result = new HashSet<>();
        names.forEach(name -> result.add(requireRole(name)));
        return Set.copyOf(result);
    }

    /** Rejects empty, unknown, or stale person references. */
    public void validateReferences(Collection<Role> references) {
        requireNonNull(references);
        checkArgument(!references.isEmpty(), MESSAGE_ROLES_REQUIRED);
        checkArgument(roles.containsAll(references), "Person references an unknown or stale role.");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RoleCatalog catalog && roles.equals(catalog.roles);
    }

    @Override
    public int hashCode() {
        return roles.hashCode();
    }
}
