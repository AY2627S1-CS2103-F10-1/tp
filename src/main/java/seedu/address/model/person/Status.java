package seedu.address.model.person;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Represents a candidate's recruitment status.
 */
public enum Status {
    APPLIED("Applied"),
    SHORTLISTED("Shortlisted"),
    INTERVIEWING("Interviewing"),
    OFFERED("Offered"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    WITHDRAWN("Withdrawn");

    public static final String MESSAGE_CONSTRAINTS = "Status must be one of: "
            + Arrays.stream(values()).map(Status::toString).collect(Collectors.joining(", "));

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the status matching the input, ignoring case.
     *
     * @throws IllegalArgumentException if the input is not a valid status
     */
    public static Status fromString(String input) {
        for (Status status : values()) {
            if (status.displayName.equalsIgnoreCase(input)) {
                return status;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
