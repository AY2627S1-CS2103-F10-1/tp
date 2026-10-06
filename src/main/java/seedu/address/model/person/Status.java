package seedu.address.model.person;

/**
 * Represents a candidate's status in the hiring pipeline.
 */
public enum Status {
    APPLIED("Applied"),
    INTERVIEWING("Interviewing"),
    OFFERED("Offered"),
    REJECTED("Rejected"),
    HIRED("Hired");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the Status enum corresponding to the given string.
     * @param value the string representation of the status
     * @return the corresponding Status enum
     * @throws IllegalArgumentException if the value does not match any status
     */
    public static Status fromString(String value) {
        for (Status status : Status.values()) {
            if (status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + value);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
