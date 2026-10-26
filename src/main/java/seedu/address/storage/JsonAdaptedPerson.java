package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalog;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String status;
    private final String note;
    private final List<String> roles;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("tags") List<JsonAdaptedTag> tags, @JsonProperty("status") String status,
            @JsonProperty("note") String note, @JsonProperty("roles") List<String> roles) {
        this.roles = roles;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.status = status;
        this.note = note;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /** Constructs a legacy record without role references. */
    public JsonAdaptedPerson(String name, String phone, String email, String address,
            List<JsonAdaptedTag> tags, String status, String note) {
        this(name, phone, email, address, tags, status, note, null);
    }

    /**
     * Constructs a person without status or note for existing callers and older data.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address,
            List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, tags, null, null);
    }

    /**
     * Constructs a person with a note and the default status.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address,
            List<JsonAdaptedTag> tags, String note) {
        this(name, phone, email, address, tags, null, note);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        roles = source.getRoles().stream().map(Role::getId).sorted().toList();
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        status = source.getStatus().toString();
        note = source.getNote().map(candidateNote -> candidateNote.value).orElse(null);
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        return toModelType(new RoleCatalog());
    }

    /** Resolves persisted IDs against the complete validated catalog. */
    public Person toModelType(RoleCatalog catalog) throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException("Candidate's tags contain a null entry.");
            }
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        final Status modelStatus;
        try {
            modelStatus = status == null ? Status.APPLIED : Status.fromString(status.strip());
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(Status.MESSAGE_CONSTRAINTS);
        }

        final Set<Tag> modelTags = new HashSet<>(personTags);
        return new Person(modelName, modelPhone, modelEmail, modelAddress, modelTags, modelStatus,
                toModelNote(), toModelRoles(catalog));
    }

    private Set<Role> toModelRoles(RoleCatalog catalog) throws IllegalValueException {
        if (roles == null) {
            return Set.of(Role.NO_ROLE);
        }
        if (roles.isEmpty() || roles.stream().anyMatch(Objects::isNull)
                || new HashSet<>(roles).size() != roles.size()) {
            throw new IllegalValueException("Candidate roles must contain distinct, non-null role IDs.");
        }
        Set<Role> resolved = new HashSet<>();
        for (String id : roles) {
            Role role = catalog.getRoles().stream().filter(entry -> entry.getId().equals(id))
                    .findFirst().orElseThrow(() -> new IllegalValueException("Unknown role reference: " + id));
            resolved.add(role);
        }
        return resolved;
    }

    /**
     * Converts the optional note, accepting absent notes in legacy files and validating any supplied text.
     */
    private Optional<Note> toModelNote() throws IllegalValueException {
        if (note == null || note.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new Note(note));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

}
