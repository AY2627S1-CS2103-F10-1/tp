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
            checkArgument(names.add(Role.normalize(role.getName())), "A role with this name already exists.");
        }
        checkArgument(roles.contains(Role.NO_ROLE), "The catalog must contain No role.");
        this.roles = roles.stream().sorted(Comparator.comparing(role -> Role.normalize(role.getName()))).toList();
    }

    public List<Role> getRoles() {
        return roles;
    }

    /** Finds a role by its trimmed, case-insensitive name. */
    public Optional<Role> find(String name) {
        String key = Role.normalize(name);
        return roles.stream().filter(role -> Role.normalize(role.getName()).equals(key)).findFirst();
    }

    /** Resolves an existing name, rejecting unknown assignments. */
    public Role requireRole(String name) {
        return find(name).orElseThrow(() -> new IllegalArgumentException("Role does not exist: " + name.strip()));
    }

    /** Resolves a nonempty set of names through the same lookup used by management commands. */
    public Set<Role> resolve(Collection<String> names) {
        requireNonNull(names);
        checkArgument(!names.isEmpty(), "Every person must have at least one role.");
        Set<Role> result = new HashSet<>();
        names.forEach(name -> result.add(requireRole(name)));
        return Set.copyOf(result);
    }

    /** Rejects empty, unknown, or stale person references. */
    public void validateReferences(Collection<Role> references) {
        requireNonNull(references);
        checkArgument(!references.isEmpty(), "Every person must have at least one role.");
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
